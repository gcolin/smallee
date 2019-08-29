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

package net.gcolin.common.reflect;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * An {@code InvocationHandler} with a {@code Type}.
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 */
public class TypedInvocationHandler implements InvocationHandler {

  private InvocationHandler ih;
  private Class<?> type;

  public TypedInvocationHandler(InvocationHandler ih, Class<?> type) {
    this.ih = ih;
    this.type = type;
  }

  public Class<?> getType() {
    return type;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return ih.invoke(proxy, method, args);
  }

  /**
   * Get the type of the proxy if the proxy 
   * has a TypedInvocationHandler.
   * 
   * @param obj a proxy
   * @return the real type or the class of the proxy
   */
  public static Class<?> getRealType(Object obj) {
    Class<?> type = obj.getClass();
    if (Proxy.isProxyClass(type)) {
      InvocationHandler ih = Proxy.getInvocationHandler(obj);
      if (ih instanceof TypedInvocationHandler) {
        type = ((TypedInvocationHandler) ih).getType();
      }
    }
    return type;
  }

  /**
   * Extend a proxy class with a list of interfaces.
   * 
   * @param obj an input object
   * @param intf interfaces to add
   * @return an extended proxy or the input object
   */
  public static Object extend(Object obj, Class<?>... intf) {
    if (Proxy.isProxyClass(obj.getClass())) {
      return Proxy.newProxyInstance(obj.getClass().getClassLoader(), intf,
          Proxy.getInvocationHandler(obj));
    } else {
      return obj;
    }
  }
}
