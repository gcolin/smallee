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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class TestPrototype {

  @Singleton
  public static class A {
    @Inject
    B b;
  }

  public static class B {
    int i = 0;

    public int getI() {
      return i;
    }

    public void setI(int i) {
      this.i = i;
    }
  }

  public static class C extends A {
    double rand = Math.random();

    public double get() {
      return rand;
    }
  }

  @Test
  public void test() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, B.class, C.class);

    A a = env.get(A.class);
    assertNotNull(a);
    a.b.setI(1);

    assertEquals(1, env.get(A.class).b.getI());
    B b = env.get(A.class).b;
    assertNotNull(b);
    assertTrue(env.get(C.class).get() != env.get(C.class).get());
  }
}
