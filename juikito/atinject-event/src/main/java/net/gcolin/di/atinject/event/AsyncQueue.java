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

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import net.gcolin.di.atinject.jmx.Jmx;
import net.gcolin.di.atinject.jmx.JmxAttribute;

/**
 * A queue for an asynchronous observer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class AsyncQueue implements Queue<Runnable> {

  private Executor executor;
  private Queue<Runnable> delegate;
  private AtomicInteger running = new AtomicInteger(0);
  @Jmx
  private int size;
  @JmxAttribute
  private String name;
  private ReentrantLock lock = new ReentrantLock();

  public AsyncQueue(String name, Executor executor, Queue<Runnable> delegate, int size) {
    this.name = name;
    this.executor = executor;
    this.delegate = delegate;
    this.size = size;
  }

  @Override
  public boolean offer(Runnable element) {
    lock.lock();
    try {
      boolean result = delegate.offer(element);
      if (delegate.size() > 0 && running.get() < size) {
        running.incrementAndGet();
        executor.execute(() -> {
          while(true) {
            lock.lock();
            Runnable current;
            try {
              if(delegate.isEmpty()) {
                running.decrementAndGet();
                return;
              }
              current = delegate.poll();
            } finally {
              lock.unlock();
            }
            current.run();
          }
        });
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Runnable poll() {
    throw new UnsupportedOperationException();
  }
  
  @Override
  public int size() {
    return size;
  }
  
  @Jmx
  public int getPendingSize() {
    lock.lock();
    try {
      return delegate.size();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<Runnable> iterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object[] toArray() {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends Runnable> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean add(Runnable e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Runnable remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Runnable element() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Runnable peek() {
    throw new UnsupportedOperationException();
  }

}
