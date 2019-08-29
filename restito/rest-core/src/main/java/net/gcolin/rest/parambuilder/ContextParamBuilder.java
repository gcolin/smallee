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

package net.gcolin.rest.parambuilder;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.core.Key;
import net.gcolin.rest.BindingEnvironment;
import net.gcolin.rest.param.ContextResolverParam;
import net.gcolin.rest.param.Param;
import net.gcolin.rest.param.ParamBridge;
import net.gcolin.rest.param.ParamParamProviderBridge;
import net.gcolin.rest.param.ParamProviderBridge;
import net.gcolin.rest.param.SingletonParam;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.provider.SingletonSupplier;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;

/**
 * Build a ContextParam.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see Context
 *
 */
public class ContextParamBuilder implements ParamBuilder {

  private SimpleProviders providers;

  public ContextParamBuilder(SimpleProviders providers) {
    this.providers = providers;
  }

  @Override
  public Param build(Class<?> type, Type genericType, Annotation[] annotations, boolean multipart,
      Annotation annotation) {
    if (type == ContextResolver.class) {
      Class<?> resolverType = Reflect
          .getTypeArguments(ContextResolver.class, genericType, type.getEnclosingClass()).get(0);
      if (!providers.isMediatypeContextResolver(resolverType)) {
        return new SingletonParam(providers.getContextResolver(resolverType, null));
      }
      return new ContextResolverParam(resolverType, providers);
    }
    Class<?> lookupClass = type;
    Type lookupGenericType = genericType;
    BindingEnvironment<Supplier<Object>> lookupContext = providers.getContextProviders();
    if (type == Supplier.class) {
      Type resolverType = Reflect
          .getGenericTypeArguments(Supplier.class, genericType, type.getEnclosingClass()).get(0);
      lookupClass = Reflect.toClass(resolverType);
      lookupGenericType = resolverType;
      lookupContext = providers.getContextMetaProviders();
    }

    Param param = find0(lookupContext, lookupClass, lookupGenericType, annotations);

    if (param != null) {
      return param;
    }

    if (lookupGenericType instanceof ParameterizedType) {
      ParameterizedType pt = (ParameterizedType) lookupGenericType;
      for (Type t : pt.getActualTypeArguments()) {
        if ("?".equals(t.getTypeName())) {
          param = find0(lookupContext, lookupClass, lookupClass, annotations);
          if (param != null) {
            return param;
          }
          break;
        }
      }

    }
    throw new IllegalArgumentException("cannot find context provider for " + genericType);
  }

  private Param find0(BindingEnvironment<Supplier<Object>> lookupContext, Class<?> lookupClass,
      Type lookupGenericType, Annotation[] annotations) {
    Key key = lookupContext.createKey(lookupClass, lookupGenericType,
        lookupContext.findQualifiers(annotations));
    Supplier<Object> provider = lookupContext.resolveBinding(key);

    if (provider != null) {
      return buildParam(provider);
    } else if (lookupContext == providers.getContextMetaProviders()) {
      provider = providers.getContextProviders().resolveBinding(key);
      if (provider != null) {
        return buildParamProviderBridge(provider);
      }
    }
    return null;
  }

  private Param buildParam(Supplier<Object> provider) {
    if (isSingletonParam(provider)) {
      return (Param) provider.get();
    } else {
      return new ParamBridge(provider);
    }
  }

  private Param buildParamProviderBridge(Supplier<Object> provider) {
    if (isSingletonParam(provider)) {
      return new ParamParamProviderBridge((Param) provider.get());
    } else {
      return new ParamProviderBridge(provider);
    }
  }

  private boolean isSingletonParam(Supplier<Object> provider) {
    return provider instanceof SingletonSupplier && provider.get() instanceof Param;
  }

}
