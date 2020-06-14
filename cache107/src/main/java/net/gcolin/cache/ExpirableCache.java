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

import java.util.Comparator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

/**
 * A partial cache implementation with the eviction implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 * @param <E> the type of cache item
 */
public abstract class ExpirableCache<E extends Expirable> implements BiConsumer<String, Object> {

  protected ReentrantLock lock = new ReentrantLock();
  private int maxSize = -1;
  protected volatile long nextTick = -1;
  protected ConcurrentSortedCollection<E> evictionList;
  private CleanUpStrategy evictionStrategy = new DefaultCleanUpStrategy();

  /**
   * Create an ExpirableCache.
   */
  public ExpirableCache() {
    evictionList = new ConcurrentSortedCollection<>(new Comparator<E>() {

      @Override
      public int compare(E o1, E o2) {
        long delta = o2.expire - o1.expire;
        if (delta == 0) {
          return 0;
        } else if (delta < 0) {
          return -1;
        }
        return 1;
      }
    });
  }

  /**
   * Set the maximum size of the cache.
   * 
   * @param maxSize the maximum size of the cache
   */
  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
    if (maxSize == -1) {
      if (evictionStrategy.getClass() == MaxSizeCleanUpStrategy.class) {
        evictionStrategy = new DefaultCleanUpStrategy();
      }
    } else {
      if (evictionStrategy.getClass() == DefaultCleanUpStrategy.class) {
        evictionStrategy = new MaxSizeCleanUpStrategy();
      }
    }
  }

  public int getMaxSize() {
    return maxSize;
  }

  protected void cleanUp() {
    evictionStrategy.cleaup();
  }

  protected abstract void evict(E element);

  interface CleanUpStrategy {
    void cleaup();
  }

  class DefaultCleanUpStrategy implements CleanUpStrategy {

    @Override
    public void cleaup() {
      if (nextTick != -1 && nextTick <= System.currentTimeMillis()) {
        long current = System.currentTimeMillis();
        // lock for performance only
        lock.lock();
        try {
          if (nextTick <= current) {
            cleanup0(current);
          }
        } finally {
          lock.unlock();
        }
      }
    }

    public void cleanup0(long current) {
      while (!evictionList.isEmpty() && evictionList.getTail().element.expire <= current) {
        E item = evictionList.getTail().element;
        evict(item);
      }
      if (evictionList.isEmpty()) {
        nextTick = -1;
      } else {
        long futureTick = evictionList.getTail().element.expire;
        if (futureTick == Long.MAX_VALUE) {
          nextTick = -1;
        } else {
          nextTick = futureTick;
        }
      }
    }
  }

  class MaxSizeCleanUpStrategy extends DefaultCleanUpStrategy implements CleanUpStrategy {

    @Override
    public void cleaup() {
      if (nextTick != -1) {
        super.cleaup();
      }
      if (maxSize < evictionList.size()) {
        // lock for performance only
        lock.lock();
        try {
          while (maxSize < evictionList.size()) {
            E item = evictionList.getTail().element;
            evict(item);
          }
        } finally {
          lock.unlock();
        }
      }
    }

  }


  @Override
  public void accept(String paramName, Object paramValue) {
    if ("maxSize".equals(paramName)) {
      maxSize = (Integer) paramValue;
    }
  }
}
