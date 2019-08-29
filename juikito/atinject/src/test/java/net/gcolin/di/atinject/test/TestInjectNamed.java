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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Test;

import net.gcolin.di.atinject.Environment;

import static org.junit.Assert.*;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestInjectNamed {

  @Singleton
  public static class A {

    @Inject
    @Named("the best B")
    B b;
  }

  public static interface B {

  }

  @Singleton
  public static class B1 implements B {

  }

  @Named("the best B")
  @Singleton
  public static class B2 implements B {

  }

  @Test
  public void test() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, B1.class, B2.class);

    A a = env.get(A.class);
    assertNotNull(a);
    B2 b = env.get(B2.class);
    assertNotNull(b);

    assertTrue(b == a.b);

    assertNotNull(env.get(B1.class));
  }
}
