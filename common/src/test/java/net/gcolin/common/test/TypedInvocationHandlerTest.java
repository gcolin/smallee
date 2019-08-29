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

package net.gcolin.common.test;

import net.gcolin.common.reflect.TypedInvocationHandler;

import org.junit.Assert;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

public class TypedInvocationHandlerTest {

  @SuppressWarnings({"unchecked"})
  @org.junit.Test
  public void simpleTest() {
    Supplier<Supplier<String>> supplier = () -> () -> "hello";

    Supplier<String> sup =
        (Supplier<String>) Proxy.newProxyInstance(TypedInvocationHandlerTest.class.getClassLoader(),
            new Class[] {Supplier.class}, new TypedInvocationHandler(new InvocationHandler() {

              @Override
              public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return method.invoke(supplier.get(), args);
              }
            }, Supplier.class));

    TypedInvocationHandler handler = (TypedInvocationHandler) Proxy.getInvocationHandler(sup);
    Assert.assertEquals(Supplier.class, handler.getType());
    Assert.assertEquals(Supplier.class, TypedInvocationHandler.getRealType(sup));
    Assert.assertEquals("hello", sup.get());

    Assert.assertNotEquals(sup, TypedInvocationHandler.extend(sup, Supplier.class));
    Assert.assertEquals(supplier, TypedInvocationHandler.extend(supplier, Supplier.class));
  }

}
