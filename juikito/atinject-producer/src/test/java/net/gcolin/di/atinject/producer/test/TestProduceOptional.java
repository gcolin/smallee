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
package net.gcolin.di.atinject.producer.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.Test;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.producer.Produces;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestProduceOptional {

  @Singleton
  public static class A {
    @Inject
    OptionalInt hello;
    
    @Inject
    OptionalLong hello2;
    
    @Inject
    OptionalDouble hello3;
  }

  @Singleton
  public static class B {

    @Produces
    int hello = 1;
    
    @Produces
    long hello2 = 2;
    
    @Produces
    double hello3 = 3;
    
  }
  
  @Singleton
  public static class C {

    @Produces
    Integer hello = 1;
    
    @Produces
    Long hello2 = 2L;
    
    @Produces
    Double hello3 = 3.0;
    
  }

  @Test
  public void testPresentPrimitive() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, B.class);
    env.start();

    A a = env.get(A.class);
    assertTrue(a.hello.isPresent());
    assertTrue(a.hello2.isPresent());
    assertTrue(a.hello3.isPresent());
    
    assertEquals(1, a.hello.getAsInt());
    assertEquals(2, a.hello2.getAsLong());
    assertEquals(3, a.hello3.getAsDouble(), 0.1);
  }
  
  @Test
  public void testPresentObject() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, C.class);
    env.start();

    A a = env.get(A.class);
    assertTrue(a.hello.isPresent());
    assertTrue(a.hello2.isPresent());
    assertTrue(a.hello3.isPresent());
    
    assertEquals(1, a.hello.getAsInt());
    assertEquals(2, a.hello2.getAsLong());
    assertEquals(3, a.hello3.getAsDouble(), 0.1);
  }

  @Test
  public void testOptional() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class);
    env.start();

    A a = env.get(A.class);
    assertFalse(a.hello.isPresent());
    assertFalse(a.hello2.isPresent());
    assertFalse(a.hello3.isPresent());
  }

}
