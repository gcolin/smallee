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

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
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
public class AsyncTest {

  private static Thread thread;
  
  @Singleton
  public static class A {
    
    void listen(@Async @Observes Evt evt) {
      Assert.assertNotEquals(thread, Thread.currentThread());
      evt.count.countDown();
    }
    
    void listen2(@Observes Evt evt) {
      Assert.assertEquals(thread, Thread.currentThread());
      evt.count.countDown();
    }
    
  }
  
  public static class Evt {
    
    private CountDownLatch count = new CountDownLatch(2);
    
  }
  
  public static class B {
    
    @Inject
    Event<Evt> event;
    
  }
  
  @Test
  public void test() throws InterruptedException {
    thread = Thread.currentThread();
    Environment env = new Environment();
    env.add(A.class, B.class);
    env.start();
    Evt evt = new Evt();
    env.get(B.class).event.fire(evt);
    
    evt.count.await();
    env.stop();
  }
  
}
