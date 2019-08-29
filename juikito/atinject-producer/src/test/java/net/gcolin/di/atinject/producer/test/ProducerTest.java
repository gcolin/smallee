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

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.producer.Produces;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ProducerTest {

  public static class A {

    @Singleton
    @Produces
    @Named("msg")
    String get() {
      return "hello";
    }

    @Produces
    @Named("msg2")
    String msg2 = "world";
  }

  @Test
  public void testSimple() {
    Environment env = new Environment();
    env.add(A.class);
    env.start();
    Assert.assertEquals("hello", env.find("msg"));
    Assert.assertEquals("world", env.find("msg2"));
  }

}
