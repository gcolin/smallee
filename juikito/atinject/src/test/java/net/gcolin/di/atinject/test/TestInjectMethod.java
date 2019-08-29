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
public class TestInjectMethod {

  @Singleton
  public static class A {
    @Inject
    public void init(B b, C c) {
      this.b = b;
      this.c = c;
    }

    @Inject
    public void setD(D d) {
      this.d = d;
    }

    B b;
    C c;
    D d;
  }

  @Singleton
  public static class B {

  }

  @Singleton
  public static class C {

  }

  @Singleton
  public static class D {

  }

  @Test
  public void test() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, B.class, C.class, D.class);

    A a = env.get(A.class);
    assertNotNull(a);
    assertSame(env.get(D.class), a.d);
    B b = env.get(B.class);
    assertNotNull(b);
    C c = env.get(C.class);
    assertNotNull(c);

    assertTrue(b == a.b);
    assertTrue(c == a.c);
  }
}
