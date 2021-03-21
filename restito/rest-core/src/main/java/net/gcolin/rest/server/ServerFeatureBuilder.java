/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.rest.server;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.ws.rs.NameBinding;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.common.reflect.TypedInvocationHandler;
import net.gcolin.rest.Environment;
import net.gcolin.rest.FeatureBuilder;
import net.gcolin.rest.RestConfiguration;
import net.gcolin.rest.router.Router;

/**
 * A class for enabling all features of the REST server side.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServerFeatureBuilder extends FeatureBuilder implements FeatureContext {

  private Router<ResourceArray> router;
  private RestContainer restContainer;
  private ServerProviders providers;
  private List<AbstractResource> resources = new ArrayList<AbstractResource>();

  /**
   * Create a ServerFeatureBuilder.
   * 
   * @param restContainer a container
   * @param providers rest providers
   * @param router a router
   * @param restConfiguration a configuration
   * @param env a bean provider
   */
  public ServerFeatureBuilder(RestContainer restContainer, ServerProviders providers,
      Router<ResourceArray> router, RestConfiguration restConfiguration, Environment env) {
    super(restConfiguration, providers, env);
    this.providers = providers;
    this.restContainer = restContainer;
    this.router = router;
  }

  @Override
  protected void enableExtras() {
    for (ParamConverterProvider item : getInstances(ParamConverterProvider.class)) {
      providers.add(item);
    }
    for (Class<?> c : getConfiguration().getClasses()) {
      if (c.isAnnotationPresent(Path.class)) {
        resources.addAll(new ResourceBuilder(providers, router, getEnvironment()).build(null, c));
      }
    }

    for (Object o : getConfiguration().getInstances()) {
      if (o.getClass().isAnnotationPresent(Path.class)) {
        resources.addAll(
            new ResourceBuilder(providers, router, getEnvironment()).build(o, o.getClass()));
      } else if (o instanceof Builder) {
        resources
            .addAll(new ResourceBuilder(providers, router, getEnvironment()).build((Builder) o));
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void enableContainerFilters() {
    for (ContainerRequestFilter item : getInstances(ContainerRequestFilter.class)) {
      Class<?> type = TypedInvocationHandler.getRealType(item);
      if (type.isAnnotationPresent(PreMatching.class)) {
        restContainer.addPreMatchingFilter(item);
      } else if (Function.class.isAssignableFrom(type)
          && Reflect.exists(type, "apply", AbstractResource.class, AbstractResource.class)) {
        doOnResources(type, (Function<AbstractResource, AbstractResource>) TypedInvocationHandler
            .extend(item, ContainerRequestFilter.class, Function.class));
      } else {
        doOnResources(type,
            r -> (r instanceof ContainerRequestFilterResource
                ? (ContainerRequestFilterResource) r
                : new ContainerRequestFilterResource(r, providers)).add(item));
      }
    }
    for (ContainerResponseFilter item : getInstances(ContainerResponseFilter.class)) {
      Class<?> type = TypedInvocationHandler.getRealType(item);
      doOnResources(type, r -> r.addFilter(item));
    }
  }

  @Override
  protected void enableInterceptors() {
    for (WriterInterceptor item : getInstances(WriterInterceptor.class)) {
      Class<?> type = TypedInvocationHandler.getRealType(item);
      doOnResources(type, r -> r.addInterceptor(item));
    }
    for (ReaderInterceptor item : getInstances(ReaderInterceptor.class)) {
      Class<?> type = TypedInvocationHandler.getRealType(item);
      doOnResources(type, r -> r.addInterceptor(item));
    }
  }

  private void doOnResources(Class<?> cf,
      Function<AbstractResource, AbstractResource> transformer) {
    Set<Class<? extends Annotation>> qualifiers = findQualifiers(cf);

    Predicate<AbstractResource> accept;
    if (qualifiers.isEmpty()) {
      accept = r -> true;
    } else {
      accept = r -> {
        Set<Class<? extends Annotation>> found = new HashSet<>();
        for (Annotation a : r.getResourceMethod().getAnnotations()) {
          if (qualifiers.contains(a.annotationType())) {
            found.add(a.annotationType());
          }
        }
        for (Annotation a : r.getResourceClass().getAnnotations()) {
          if (qualifiers.contains(a.annotationType())) {
            found.add(a.annotationType());
          }
        }
        return found.size() == qualifiers.size();
      };
    }
    for (int i = 0, l = resources.size(); i < l; i++) {
      if (accept.test(resources.get(i))) {
        resources.set(i, transformer.apply(resources.get(i)));
      }
    }
  }

  private Set<Class<? extends Annotation>> findQualifiers(Class<?> cf) {
    Set<Class<? extends Annotation>> qualifiers = new HashSet<>();
    for (Annotation a : cf.getAnnotations()) {
      if (a.annotationType().isAnnotationPresent(NameBinding.class)) {
        qualifiers.add(a.annotationType());
      }
    }
    return qualifiers;
  }

  @Override
  protected void enableDynamicFeatures() {
    for (DynamicFeature item : getInstances(DynamicFeature.class)) {
      for (AbstractResource r : resources) {
        ServerFeatureBuilder fb = new ServerFeatureBuilder(restContainer, providers, router,
            (RestConfiguration) getConfiguration(), getEnvironment());
        fb.getResources().add(r);
        item.configure(r, fb);
        fb.build();
      }
    }
  }

  public List<AbstractResource> getResources() {
    return resources;
  }

}
