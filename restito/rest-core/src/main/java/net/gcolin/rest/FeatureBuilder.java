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

package net.gcolin.rest;

import net.gcolin.common.collection.Func;
import net.gcolin.common.lang.Pair;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.common.reflect.TypedInvocationHandler;
import net.gcolin.rest.provider.SimpleProviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * A class for enabling all features.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class FeatureBuilder extends ConfigurableImpl<FeatureContext>
    implements
      FeatureContext {

  private SimpleProviders providers;
  private final Environment environment;

  private static final Comparator<Pair<Integer, ?>> PAIR_COMPARATOR =
      (o1, o2) -> o1.getKey() - o2.getKey();

  /**
   * Create a FeatureBuilder.
   * 
   * @param configuration a configuration
   * @param providers the provider registry
   * @param environment the instance factory
   */
  public FeatureBuilder(RestConfiguration configuration, SimpleProviders providers,
      Environment environment) {
    super(configuration);
    this.providers = providers;
    this.environment = environment;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public SimpleProviders getProviders() {
    return providers;
  }

  private int enableFeatures(List<Feature> all, Set<Class<?>> used) {
    int nb = 0;
    for (Feature item : all) {
      if (!used.contains(item.getClass())) {
        used.add(item.getClass());
        nb++;
        item.configure(this);
      }
    }
    return nb;
  }

  @SuppressWarnings("rawtypes")
  private void enableProviders() {
    for (ContextResolver<?> item : getInstances(ContextResolver.class)) {
      Class<?> type = TypedInvocationHandler.getRealType(item);
      providers.add(item,
          (Class<?>) Reflect.getTypeArguments(ContextResolver.class, (Class) type, null).get(0));
    }

    for (Supplier<?> item : getInstances(Supplier.class)) {
      providers.add(item);
    }
  }

  protected abstract void enableExtras();

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void enableResources() {
    for (ExceptionMapper item : getInstances(ExceptionMapper.class)) {
      providers.add(item);
    }

    for (MessageBodyWriter<?> item : getInstances(MessageBodyWriter.class)) {
      providers.add(item);
    }

    for (MessageBodyReader<?> item : getInstances(MessageBodyReader.class)) {
      providers.add(item);
    }
  }

  protected abstract void enableContainerFilters();

  protected abstract void enableInterceptors();

  protected void enableDynamicFeatures() {}

  /**
   * Enable all.
   */
  public void build() {
    if (getConfiguration().getInstances().isEmpty() && getConfiguration().getClasses().isEmpty()) {
      return;
    }
    Set<Class<?>> used = new HashSet<>();
    while (true) {
      if (enableFeatures(getInstances(Feature.class), used) == 0) {
        break;
      }
    }
    enableProviders();
    enableExtras();
    enableResources();

    enableContainerFilters();
    enableInterceptors();
    enableDynamicFeatures();
  }

  @SuppressWarnings("unchecked")
  protected <T> List<T> getInstances(Class<T> contractType) {
    List<Pair<Integer, T>> instances = new ArrayList<>();
    Configuration config = getConfiguration();
    for (Class<?> c : config.getClasses()) {
      Map<Class<?>, Integer> contracts = config.getContracts(c);
      Integer priority = contracts.get(contractType);
      if (priority != null) {
        instances.add(new Pair<Integer, T>(priority,
            (T) environment.getProvider(c, contractType, true).get()));
      }
    }
    for (Object o : config.getInstances()) {
      Map<Class<?>, Integer> contracts = config.getContracts(o.getClass());
      Integer priority = contracts.get(contractType);
      if (priority != null) {
        instances.add(new Pair<Integer, T>(priority, (T) o));
      }
    }
    Collections.sort(instances, PAIR_COMPARATOR);
    return Func.map(instances, Pair::getRight);
  }

}
