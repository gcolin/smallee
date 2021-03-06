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
package net.gcolin.di.atinject.interceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.gcolin.di.atinject.InstanceFactory;
import net.gcolin.di.core.InjectException;

/**
 * InvocationContext for intercepting a controller.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ConstructInvocationContext extends AbstractInvocationContext {

  private InstanceFactory factory;
  private Class<?> type;
  
  public ConstructInvocationContext(InstanceFactory factory,
      Class<?> type) {
    this.factory = factory;
    this.type = type;
    setParameters(factory == null ? new Object[0] : factory.getArguments());
  }

  @Override
  public Method getMethod() {
    return null;
  }

  @Override
  public Constructor<?> getConstructor() {
    return factory.getConstructor();
  }

  @Override
  public Object proceed() throws Exception {
    if (factory == null) {
      try {
        target = type.getDeclaredConstructor().newInstance();
        return target;
      } catch (Exception e) {
        throw new InjectException("cannot create " + type, e);
      }
    } else {
      target = factory.create(getParameters());
      return target;
    }
  }

}
