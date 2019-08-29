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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import javax.inject.Provider;

import net.gcolin.common.reflect.Reflect;

/**
 * Resolve providers or suppliers.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ProviderResolver implements Resolver {

  private Environment environment;
  
  public ProviderResolver(Environment environment) {
    this.environment = environment;
  }

  @Override
  public AbstractProvider<Object> find(Class<?> clazz, Type genericType, Annotation[] qualifiers) {
    if(clazz == Provider.class || clazz == Supplier.class) {
      Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
      @SuppressWarnings("unchecked")
      AbstractProvider<Object> delegate =
          (AbstractProvider<Object>) environment.getProvider(Reflect.toClass(type), type, qualifiers);
      AbstractProvider<Object> p = new SingletonProvider<Object>(delegate, genericType);
      return p;
    }
    return null;
  }

}
