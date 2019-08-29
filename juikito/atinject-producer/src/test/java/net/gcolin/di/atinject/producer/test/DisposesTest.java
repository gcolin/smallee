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

import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.producer.Disposes;
import net.gcolin.di.atinject.producer.Produces;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DisposesTest {

  public static class Resource {

    private boolean open = true;

    public boolean isOpen() {
      return open;
    }

    public void close() {
      open = false;
    }

  }

  public static class A {

    @Produces
    @Singleton
    public Resource get() {
      return new Resource();
    }

    public void dispose(@Disposes Resource res) {
      res.close();
    }

  }

  public static class B {

    @Produces
    @Singleton
    public Resource res = new Resource();

    public void dispose(@Disposes Resource res) {
      res.close();
    }

  }

  @Singleton
  public static class C {

    @Named("hello")
    @Produces
    @Singleton
    public Resource get() {
      return new Resource();
    }

    public void dispose(@Named("hello") @Disposes Resource res) {
      res.close();
    }

  }

  @Test
  public void testMethodSimple() {
    testSimple0(A.class);
  }

  @Test
  public void testMethodQualifier() {
    Environment env = new Environment();
    env.add(C.class);
    env.start();
    Resource resource = (Resource) env.find("hello");
    Assert.assertTrue(resource.isOpen());
    env.stop();
    Assert.assertFalse(resource.isOpen());
  }

  @Test
  public void testFieldSimple() {
    testSimple0(B.class);
  }

  private void testSimple0(Class<?> clazz) {
    Environment env = new Environment();
    env.add(clazz);
    env.start();
    Resource resources = env.get(Resource.class);
    Assert.assertTrue(resources.isOpen());
    env.stop();
    Assert.assertFalse(resources.isOpen());
  }

}
