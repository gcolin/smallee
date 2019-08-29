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

import javax.annotation.Priority;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestPriority {

  public static interface Service {
    
  }
  
  public static class A implements Service {
    
  }
  
  @Priority(10)
  public static class B implements Service {
    
  }
  
  @Priority(20)
  public static class C implements Service {
    
  }
  
  @Test
  public void test() {
    test0(B.class, A.class, B.class);
    test0(B.class, B.class, A.class);
    test0(C.class, A.class, C.class);
    test0(C.class, C.class, A.class);
    test0(B.class, C.class, B.class, A.class);
    test0(B.class, A.class, B.class, C.class);
  }
  
  private void test0(Class<?> expected, Class<?>...classes) {
    Environment env = new Environment();
    env.add(classes);
    env.start();
    Assert.assertEquals(expected ,env.get(Service.class).getClass());
  }
  
}
