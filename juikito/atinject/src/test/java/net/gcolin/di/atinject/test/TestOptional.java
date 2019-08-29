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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Test;

import net.gcolin.di.atinject.Environment;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestOptional {

  @Singleton
  public static class A {
    @Inject
    Optional<B> b;
    
    @Named("pickme")
    @Inject
    Optional<Collection<String>> c;
  }

  @Singleton
  public static class B {

  }
  
  @Named("pickme")
  @Singleton
  public static class C extends ArrayList<String> {

    private static final long serialVersionUID = 1L;

  }
  
  @Singleton
  public static class D extends ArrayList<String> {

    private static final long serialVersionUID = 1L;

  }

  @Test
  public void testPresent() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, B.class);

    A a = env.get(A.class);
    assertTrue(a.b.isPresent());
  }

  @Test
  public void testOptional() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class);

    A a = env.get(A.class);
    assertFalse(a.b.isPresent());
  }
  
  @Test
  public void testQualifier() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, D.class, C.class);

    A a = env.get(A.class);
    assertTrue(a.c.isPresent());
    assertTrue(a.c.get() instanceof C);
  }

}
