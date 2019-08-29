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
package net.gcolin.di.atinject.web;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.ProviderBuilder;

/**
 * Request scoped builder.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class RequestProviderBuilder implements ProviderBuilder {

Class<? extends Annotation> scope;
  
  public RequestProviderBuilder(Class<? extends Annotation> scope) {
    this.scope = scope;
  }

  @Override
  public Class<? extends Annotation> getScope() {
    return scope;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> AbstractProvider<T> create(Class<?> type, Type genericType, Class<?> resolvedClazz,
      Type resolvedGenericType, Environment env) {
    return new RequestProvider<T>((Class<T>) type, genericType, (Class<T>) resolvedClazz,
        resolvedGenericType, env);
  }

  @Override
  public <T> AbstractProvider<T> decorate(AbstractProvider<T> provider) {
    return new RequestDecorateProvider<>(provider);
  }

}
