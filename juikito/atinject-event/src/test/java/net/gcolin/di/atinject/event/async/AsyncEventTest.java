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
package net.gcolin.di.atinject.event.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.event.Async;
import net.gcolin.di.atinject.event.Event;
import net.gcolin.di.atinject.event.Observes;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class AsyncEventTest {

  @Inject
  Event<SynchronousEvent> sync;

  @Inject
  Event<AsynchronousEvent> async;

  @Inject
  @Named
  Event<AsynchronousEvent> async2;

  @Inject
  @Named("3")
  Event<AsynchronousEvent> async3;

  Environment env;

  @Singleton
  public static class A {

    @Inject
    TimeWatch watch;

    Set<String> done = Collections.synchronizedSet(new HashSet<>());

    public void oneSecondSynchronousConsumer(@Observes SynchronousEvent event)
        throws InterruptedException {
      Thread.sleep(100);
      done.add(event.getName());
      watch.update();
    }

    public void oneSecondAsynchronousConsumer(@Observes @Async AsynchronousEvent event)
        throws InterruptedException {
      Thread.sleep(100);
      done.add(event.getName());
      watch.update();
    }

    public void oneSecondAsynchronousConsumer2(
        @Observes @Async("q2") @Named AsynchronousEvent event) throws InterruptedException {
      Thread.sleep(100);
      done.add(event.getName());
      watch.update();
    }

    public void oneSecondAsynchronousConsumer3(
        @Observes @Async(value = "q3", size = 2) @Named("3") AsynchronousEvent event)
        throws InterruptedException {
      Thread.sleep(100);
      done.add(event.getName());
      watch.update();
    }
  }

  @Before
  public void before() {
    env = new Environment();
    env.add(A.class, TimeWatch.class);
    env.start();
    env.bind(this);
  }

  @After
  public void after() {
    env.stop();
  }

  @Test
  public void testSynchronousEventHandling() throws Exception {

    A a = env.get(A.class);

    assertNotNull(a.watch);
    assertNotNull(sync);

    a.watch.reset();
    sync.fire(new SynchronousEvent("Event 1"));
    sync.fire(new SynchronousEvent("Event 2"));

    assertNotNull(a.watch.time());

    assertEquals(2, a.done.size());

    assertTrue(a.watch.time() + " > 200", a.watch.time() >= 200);
    assertTrue(a.watch.time() + " < 250", a.watch.time() < 250);
  }

  @Test
  public void testAsynchronousEventHandling() throws Exception {

    A a = env.get(A.class);

    assertNotNull(a.watch);
    assertNotNull(sync);

    a.watch.reset();
    async.fire(new AsynchronousEvent("Event 1"));
    async.fire(new AsynchronousEvent("Event 2"));
    assertNotNull(a.watch.time());

    assertTrue(a.watch.time() <= 100);
    Thread.sleep(120);

    assertEquals(1, a.done.size());

    assertTrue(a.watch.time() + " > 100", a.watch.time() >= 100);
    assertTrue(a.watch.time() + " < 150", a.watch.time() < 150);

    Thread.sleep(120);

    assertEquals(2, a.done.size());
  }

  @Test
  public void testAsynchronousEventHandlingParallele() throws Exception {

    A a = env.get(A.class);

    assertNotNull(a.watch);
    assertNotNull(sync);

    a.watch.reset();
    async.fire(new AsynchronousEvent("Event 1"));
    async.fire(new AsynchronousEvent("Event 2"));
    async2.fire(new AsynchronousEvent("Event 3"));
    async2.fire(new AsynchronousEvent("Event 4"));
    assertNotNull(a.watch.time());

    assertTrue(a.watch.time() <= 100);
    Thread.sleep(120);

    assertEquals(2, a.done.size());

    assertTrue(a.watch.time() + " > 10", a.watch.time() >= 100);
    assertTrue(a.watch.time() + " < 15", a.watch.time() < 150);

    Thread.sleep(120);

    assertEquals(4, a.done.size());
  }

  @Test
  public void testAsynchronousEventHandlingParallele2() throws Exception {

    A a = env.get(A.class);

    assertNotNull(a.watch);
    assertNotNull(sync);
    
    for (int i = 0; i < 3; i++) {
      a.done.clear();
      a.watch.reset();
      async3.fire(new AsynchronousEvent("Event 1"));
      async3.fire(new AsynchronousEvent("Event 2"));
      async3.fire(new AsynchronousEvent("Event 3"));
      async3.fire(new AsynchronousEvent("Event 4"));
      assertNotNull(a.watch.time());

      assertTrue(a.watch.time() <= 100);
      Thread.sleep(120);

      assertEquals(2, a.done.size());

      assertTrue(a.watch.time() + " > 10", a.watch.time() >= 100);
      assertTrue(a.watch.time() + " < 15", a.watch.time() < 150);

      Thread.sleep(120);

      assertEquals(4, a.done.size());

    }
  }



}
