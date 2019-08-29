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
package net.gcolin.di.atinject.jndi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.annotation.Resource;
import javax.inject.Provider;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.InjectionPoint;
import net.gcolin.di.atinject.InjectionPointBuilder;

/**
 * ResourceInjectionPointBuilder.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ResourceInjectionPointBuilder implements InjectionPointBuilder {

  @Override
  public InjectionPoint create(Field field, Environment env) {
    if(field.isAnnotationPresent(Resource.class)) {
      String jndiName = JndiExtension.getJndiName(field.getAnnotation(Resource.class), field);
      if(field.getType() == Provider.class) {
        return new ProvidedResourceFieldInjectionPoint(jndiName, field);
      } else {
        return new ResourceFieldInjectionPoint(jndiName, field);
      }
    }
    return null;
  }

  @Override
  public InjectionPoint create(Method method, Environment env) {
    return null;
  }

}
