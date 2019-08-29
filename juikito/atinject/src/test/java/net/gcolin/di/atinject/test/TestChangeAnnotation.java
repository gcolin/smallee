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
package net.gcolin.di.atinject.test;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;

import net.gcolin.di.atinject.Environment;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestChangeAnnotation {

  @Target({ METHOD, CONSTRUCTOR, FIELD })
  @Retention(RUNTIME)
  @Documented
  public @interface In {}
  
  @Singleton
  public static class A {
    @In
    B b;
    
    B b2;
    
    B b3;
    
    @In
    public A(B b2) {
      this.b2 = b2;
    }
    
    @In
    void setB3(B b3) {
      this.b3 = b3;
    }
  }

  @Singleton
  public static class B {

  }
  
  @Singleton
  public static class C {
    
    @In
    B b;
    
    @Inject
    B b2;
  }

  @Test
  public void testAddIn() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, B.class);
    env.getInjectAnnotations().add(In.class);

    A a = env.get(A.class);
    assertNotNull(a);
    B b = env.get(B.class);
    assertNotNull(b);

    assertTrue(b == a.b);
    assertTrue(b == a.b2);
    assertTrue(b == a.b3);
  }
  
  @Test
  public void testInjectOnly() throws Exception {
    Environment env = new Environment();
    env.addClasses(C.class, B.class);

    C c = env.get(C.class);
    assertNotNull(c);
    B b = env.get(B.class);
    assertNotNull(b);

    assertNull(c.b);
    assertTrue(b == c.b2);
  }
  
  @Test
  public void testInOnly() throws Exception {
    Environment env = new Environment();
    env.addClasses(C.class, B.class);
    env.getInjectAnnotations().remove(Inject.class);
    env.getInjectAnnotations().add(In.class);

    C c = env.get(C.class);
    assertNotNull(c);
    B b = env.get(B.class);
    assertNotNull(b);

    assertNull(c.b2);
    assertTrue(b == c.b);
  }
}
