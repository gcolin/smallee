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
package net.gcolin.di.atinject.interceptor.test;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InvokeTest {

  static boolean interceptorCalled;
  static boolean methodCalled;
  static boolean interceptorCalledEnd;
  static boolean interceptorCalled2;
  static boolean interceptorCalledEnd2;
  static boolean interceptorCalled3;
  static boolean interceptorCalledEnd3;
  
  interface HelloService {
    void hello();
  }
  
  @Retention(RUNTIME)
  @Target(TYPE)
  @InterceptorBinding
  public static @interface Bind {
    
  }
  
  @javax.interceptor.Interceptor
  public static class Interceptor {
    
    @AroundInvoke
    public void aroundInvoke(InvocationContext ctx) throws Exception{
      interceptorCalled = true;
      Assert.assertNotNull(ctx.getTarget());
      ctx.proceed();
      Assert.assertTrue(methodCalled);
      interceptorCalledEnd = true;
    }
    
  }
  
  @javax.interceptor.Interceptor
  @Bind
  public static class Interceptor2 {
    
    @AroundInvoke
    public void aroundContruct(InvocationContext ctx) throws Exception{
      interceptorCalled2 = true;
      Assert.assertNotNull(ctx.getTarget());
      ctx.proceed();
      Assert.assertTrue(methodCalled);
      interceptorCalledEnd2 = true;
    }
    
  }
  
  @Priority(javax.interceptor.Interceptor.Priority.APPLICATION)
  @javax.interceptor.Interceptor
  @Bind
  public static class Interceptor3 {
    
    @AroundInvoke
    public void aroundContruct(InvocationContext ctx) throws Exception{
      interceptorCalled3 = true;
      Assert.assertNotNull(ctx.getTarget());
      Assert.assertFalse(interceptorCalled2);
      Assert.assertFalse(interceptorCalledEnd2);
      ctx.proceed();
      Assert.assertTrue(methodCalled);
      Assert.assertTrue(interceptorCalled2);
      Assert.assertTrue(interceptorCalledEnd2);
      interceptorCalledEnd3 = true;
    }
    
  }
  
  @Priority(javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE)
  @javax.interceptor.Interceptor
  @Bind
  public static class Interceptor4 {
    
    @AroundInvoke
    public void aroundContruct(InvocationContext ctx) throws Exception{
      interceptorCalled = true;
      Assert.assertNotNull(ctx.getTarget());
      Assert.assertFalse(interceptorCalled2);
      Assert.assertFalse(interceptorCalledEnd2);
      Assert.assertFalse(interceptorCalled3);
      Assert.assertFalse(interceptorCalledEnd3);
      ctx.proceed();
      Assert.assertTrue(methodCalled);
      Assert.assertTrue(interceptorCalled3);
      Assert.assertTrue(interceptorCalledEnd3);
      Assert.assertTrue(interceptorCalled2);
      Assert.assertTrue(interceptorCalledEnd2);
      interceptorCalledEnd = true;
    }
    
  }
  
  @Interceptors(Interceptor.class)
  public static class A implements HelloService {
    
    public void hello() {
      Assert.assertFalse(interceptorCalledEnd);
      methodCalled = true;
    }
    
  }
  
  @Bind
  public static class B implements HelloService {
    
    public void hello() {
      Assert.assertFalse(interceptorCalledEnd2);
      methodCalled = true;
    }
    
  }
  
  @Bind
  @Interceptors(Interceptor.class)
  public static class C implements HelloService {
    
    public void hello() {
      Assert.assertFalse(interceptorCalledEnd);
      Assert.assertFalse(interceptorCalledEnd2);
      methodCalled = true;
    }
    
  }
  
  @Before
  public void init() {
    interceptorCalled = false;
    interceptorCalledEnd = false;
    interceptorCalled2 = false;
    interceptorCalledEnd2 = false;
    interceptorCalled3 = false;
    interceptorCalledEnd3 = false;
    methodCalled = false;
  }
  
  @Test
  public void testInterceptorAnnotation() {
    Environment env = new Environment();
    env.add(A.class, Interceptor.class);
    env.start();
    env.get(HelloService.class).hello();
    Assert.assertTrue(interceptorCalledEnd);
  }
  
  @Test
  public void testBindingAnnotation() {
    Environment env = new Environment();
    env.add(B.class, Interceptor2.class);
    env.start();
    env.get(HelloService.class).hello();
    Assert.assertTrue(interceptorCalledEnd2);
  }
  
  @Test
  public void testManyInterceptors() {
    Environment env = new Environment();
    env.add(C.class, Interceptor.class, Interceptor2.class);
    env.start();
    env.get(HelloService.class).hello();
    Assert.assertTrue(interceptorCalledEnd);
    Assert.assertTrue(interceptorCalledEnd2);
  }
  
  @Test
  public void testInterceptorPriority() {
    Environment env = new Environment();
    env.add(B.class, Interceptor3.class, Interceptor2.class, Interceptor4.class);
    env.start();
    env.get(HelloService.class).hello();
    Assert.assertTrue(interceptorCalledEnd);
    Assert.assertTrue(interceptorCalledEnd2);
    Assert.assertTrue(interceptorCalledEnd3);
  }
  
}
