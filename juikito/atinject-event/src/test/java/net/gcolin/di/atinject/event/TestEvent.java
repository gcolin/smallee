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
package net.gcolin.di.atinject.event;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestEvent {

  public static class Evt {

    int value;

    public Evt(int value) {
      this.value = value;
    }
  }

  public static class A {

    @Inject
    Event<Evt> event;
    
    @Inject @Named
    Event<Evt> event2;
    
    public void send(int val) {
      event.fire(new Evt(val));
    }
    
    public void send2(int val) {
      event2.fire(new Evt(val));
    }

  }
  
  @Singleton
  public static class B {
    
    int val;
    int val2;
    
    void listen(@Observes Evt evt) {
      val = evt.value;
    }
    
    void listen2(@Named @Observes Evt evt) {
      val2 = evt.value;
    }

  }
  
  @Test
  public void test() {
    Environment env = new Environment();
    env.add(A.class, B.class);
    env.start();
    
    A a = env.get(A.class);
    B b = env.get(B.class);
    Assert.assertEquals(0, b.val);
    Assert.assertEquals(0, b.val2);
    a.send(10);
    Assert.assertEquals(10, b.val);
    Assert.assertEquals(0, b.val2);
    a.send(7);
    Assert.assertEquals(7, b.val);
    Assert.assertEquals(0, b.val2);
    
    a.send2(3);
    Assert.assertEquals(7, b.val);
    Assert.assertEquals(3, b.val2);
  }

}
