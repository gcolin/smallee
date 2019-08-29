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

package net.gcolin.common.test;

import net.gcolin.common.Time;
import net.gcolin.common.collection.ConcurrentQueue;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

public class ConcurrentQueueTest {

  @Test
  public void testSimple() {
    ConcurrentQueue<Long> queue = new ConcurrentQueue<>(100);
    queue.offer(1L);
    queue.remove(1L);
    Assert.assertTrue(queue.isEmpty());

    for (int j = 0; j < 100; j++) {
      queue.add((long) j);
    }

    for (int j = 99; j >= 0; j--) {
      Assert.assertEquals(j, queue.poll().intValue());
    }
  }

  @Test
  @Ignore
  public void testSpeed() {
    ConcurrentQueue<Long> queue = new ConcurrentQueue<>(100);
    Time.tick();
    for (int i = 0; i < 1000000000; i++) {
      queue.offer(1L);
      queue.poll();
    }
    Time.tock("add/remove 1000000000");

    Time.tick();
    for (int i = 0; i < 10000000; i++) {
      queue.offer(1L);
    }
    for (int i = 0; i < 10000000; i++) {
      queue.poll();
    }
    Time.tock("add 10000000 remove 10000000");

    ArrayDeque<Long> q2 = new ArrayDeque<>();
    Time.tick();
    for (int i = 0; i < 1000000000; i++) {
      q2.offerLast(1L);
      q2.pollLast();
    }
    Time.tock("ArrayDeque add/remove 1000000000");

    Time.tick();
    for (int i = 0; i < 10000000; i++) {
      q2.offerLast(1L);
    }
    for (int i = 0; i < 10000000; i++) {
      q2.pollLast();
    }
    Time.tock("ArrayDeque add 10000000 remove 10000000");
  }

  @Test
  public void testIterator() {
    Queue<Long> queue = new ConcurrentQueue<>(100);
    queue.add(1L);
    queue.offer(2L);

    Iterator<Long> it = queue.iterator();
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals(2L, it.next().longValue());
    Assert.assertTrue(it.hasNext());
    Assert.assertEquals(1L, it.next().longValue());
    Assert.assertFalse(it.hasNext());
    try {
      it.next();
      Assert.fail();
    } catch (NoSuchElementException ex) {
      // ok
    }
  }

  @Test
  public void testRemove() {
    ConcurrentQueue<Long> queue = new ConcurrentQueue<>(100);
    queue.add(1L);
    queue.offer(2L);
    queue.offer(3L);
    Assert.assertEquals(3, queue.size());
    queue.remove(2L);
    queue.remove(5L);
    Assert.assertEquals(2, queue.size());
    Assert.assertEquals(3L, queue.element().longValue());
    Assert.assertEquals(3L, queue.peek().longValue());
    Assert.assertTrue(queue.contains(1L));
    Assert.assertFalse(queue.contains(2L));
    Assert.assertTrue(queue.contains(3L));

    Assert.assertEquals(3L, queue.remove().longValue());
    Assert.assertEquals(1, queue.size());
    queue.remove();
    Assert.assertNull(queue.poll());
    Assert.assertNull(queue.peek());

    try {
      queue.remove();
      Assert.fail();
    } catch (EmptyStackException ex) {
      // ok
    }

    try {
      queue.element();
      Assert.fail();
    } catch (EmptyStackException ex) {
      // ok
    }

  }
}
