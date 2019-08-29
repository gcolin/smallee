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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Singleton;

import org.junit.Test;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Inject;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestAlternativeAnnotation {
  
  @Singleton
  public static class A {
    @Inject
    B b;
    
    B b2;
    
    B b3;
    
    @Inject
    public A(B b2) {
      this.b2 = b2;
    }
    
    @Inject
    void setB3(B b3) {
      this.b3 = b3;
    }
  }

  @Singleton
  public static class B {

  }

  @Test
  public void test() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, B.class);

    A a = env.get(A.class);
    assertNotNull(a);
    B b = env.get(B.class);
    assertNotNull(b);

    assertTrue(b == a.b);
    assertTrue(b == a.b2);
    assertTrue(b == a.b3);
  }
}
