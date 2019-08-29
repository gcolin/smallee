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

package net.gcolin.di.atinject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.inject.Singleton;

/**
 * A builder for singleton providers.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class SingletonProviderBuilder implements ProviderBuilder {

  @Override
  public Class<? extends Annotation> getScope() {
    return Singleton.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> AbstractProvider<T> create(Class<?> clazz, Type genericType, Class<?> resolvedClazz,
      Type resolvedGenericType, Environment env) {
    return new SingletonProvider<T>((Class<T>) clazz, genericType, (Class<T>) resolvedClazz,
        resolvedGenericType, env);
  }

  @Override
  public <T> AbstractProvider<T> decorate(AbstractProvider<T> provider) {
    return new SingletonDecorateProvider<>(provider);
  }


}
