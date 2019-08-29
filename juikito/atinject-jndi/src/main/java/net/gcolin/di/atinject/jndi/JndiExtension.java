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
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.annotation.Resource;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.core.InjectException;

/**
 * Enable JNDI resource annotations on fields.
 * Works lazy with a field of type Provider.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JndiExtension implements Extension {

  private static final String RESOURCE_LOOKUP_PREFIX = "java:comp/env";
  
  @Override
  public void doStart(Environment env) {
    env.addInjectionPointBuilder(new ResourceInjectionPointBuilder());
  }
  
  public static String getJndiName(Resource resource, Member member) {
    String mappedName = resource.mappedName();
    if (!mappedName.isEmpty()) {
      return mappedName;
    }
    String name = resource.name();
    if (!name.equals("")) {
      name = RESOURCE_LOOKUP_PREFIX + "/" + name;
    } else {
      String propertyName;
      if (member instanceof Field) {
        propertyName = member.getName();
      } else if (member instanceof Method) {
        propertyName = Reflect.getPropertyName((Method) member);
      } else {
        throw new InjectException("Unable to inject");
      }
      String className = member.getDeclaringClass().getName();
      name = RESOURCE_LOOKUP_PREFIX + "/" + className + "/" + propertyName;
    }
    return name;
  }

}
