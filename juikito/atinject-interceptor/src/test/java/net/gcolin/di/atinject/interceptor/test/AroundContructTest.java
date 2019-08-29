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

import javax.interceptor.AroundConstruct;
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
public class AroundContructTest {

  static boolean interceptorCalled;
  static boolean interceptorCalledEnd;
  static boolean interceptorCalled2;
  static boolean interceptorCalledEnd2;
  
  @Retention(RUNTIME)
  @Target(TYPE)
  @InterceptorBinding
  public static @interface Bind {
    
  }
  
  @javax.interceptor.Interceptor
  public static class Interceptor {
    
    @AroundConstruct
    public void aroundContruct(InvocationContext ctx) throws Exception{
      interceptorCalled = true;
      Assert.assertNull(ctx.getTarget());
      ctx.proceed();
      Assert.assertNotNull(ctx.getTarget());
      interceptorCalledEnd = true;
    }
    
  }
  
  @javax.interceptor.Interceptor
  @Bind
  public static class Interceptor2 {
    
    @AroundConstruct
    public void aroundContruct(InvocationContext ctx) throws Exception{
      interceptorCalled2 = true;
      Assert.assertNull(ctx.getTarget());
      ctx.proceed();
      Assert.assertNotNull(ctx.getTarget());
      interceptorCalledEnd2 = true;
    }
    
  }
  
  @Interceptors(Interceptor.class)
  public static class A {
    
    public A() {
      Assert.assertFalse(interceptorCalledEnd);
    }
    
  }
  
  @Bind
  public static class B {
    
    public B() {
      Assert.assertFalse(interceptorCalledEnd2);
    }
    
  }
  
  @Bind
  @Interceptors(Interceptor.class)
  public static class C {
    
    public C() {
      Assert.assertFalse(interceptorCalledEnd);
      Assert.assertFalse(interceptorCalledEnd2);
    }
    
  }
  
  @Before
  public void init() {
    interceptorCalled = false;
    interceptorCalledEnd = false;
    interceptorCalled2 = false;
    interceptorCalledEnd2 = false;
  }
  
  @Test
  public void testInterceptorAnnotation() {
    Environment env = new Environment();
    env.add(A.class, Interceptor.class);
    env.start();
    env.get(A.class);
    Assert.assertTrue(interceptorCalledEnd);
  }
  
  @Test
  public void testBindingAnnotation() {
    Environment env = new Environment();
    env.add(B.class, Interceptor2.class);
    env.start();
    env.get(B.class);
    Assert.assertTrue(interceptorCalledEnd2);
  }
  
  @Test
  public void testManyInterceptors() {
    Environment env = new Environment();
    env.add(C.class, Interceptor.class, Interceptor2.class);
    env.start();
    env.get(C.class);
    Assert.assertTrue(interceptorCalledEnd);
    Assert.assertTrue(interceptorCalledEnd2);
  }
  
}
