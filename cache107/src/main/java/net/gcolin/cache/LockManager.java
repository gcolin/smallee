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

package net.gcolin.cache;

import net.gcolin.common.collection.ArrayQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The cache concurrent lock.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LockManager<K> {

  private ReentrantLock masterLock = new ReentrantLock();
  private final Map<K, Item> locks = new HashMap<>();
  private static final int CAPACITY = 20;
  private final Queue<ReentrantReadWriteLock> lockStore = new ArrayQueue<>();

  void lockRead(K key) {
    Item item;
    masterLock.lock();
    try {
      item = getItem(key);
      item.nb++;
    } finally {
      masterLock.unlock();
    }
    item.lock.readLock().lock();
  }

  private Item getItem(K key) {
    Item item;
    item = locks.get(key);
    if (item == null) {
      ReentrantReadWriteLock lock = lockStore.poll();
      if (lock == null) {
        lock = new ReentrantReadWriteLock();
      }
      item = new Item();
      item.lock = lock;
      locks.put(key, item);
    }
    return item;
  }

  void lockWrite(K key) {
    Item item;
    masterLock.lock();
    try {
      item = getItem(key);
      item.nb++;
    } finally {
      masterLock.unlock();
    }

    item.lock.writeLock().lock();
  }

  void unlockRead(K key) {
    masterLock.lock();
    try {
      Item item = locks.get(key);
      item.nb--;
      item.lock.readLock().unlock();
      if (item.nb == 0) {
        locks.remove(key);
        if (lockStore.size() < CAPACITY) {
          lockStore.offer(item.lock);
        }
      }
    } finally {
      masterLock.unlock();
    }
  }

  void unlockWrite(K key) {
    masterLock.lock();
    try {
      Item item = locks.get(key);
      item.nb--;
      item.lock.writeLock().unlock();
      if (item.nb == 0) {
        locks.remove(key);
        if (lockStore.size() < CAPACITY) {
          lockStore.offer(item.lock);
        }
      }
    } finally {
      masterLock.unlock();
    }
  }

  private static class Item {
    private ReentrantReadWriteLock lock;
    private volatile int nb;
  }
}
