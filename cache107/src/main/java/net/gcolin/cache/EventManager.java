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

import net.gcolin.common.collection.Collections2;
import net.gcolin.common.io.Io;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.event.EventType;

/**
 * Store and call events.
 * 
 * @author Gaël COLIN
 *
 * @param <K> the type of key
 * @param <V> the type of value
 */
public class EventManager<K, V> implements Closeable {

  @SuppressWarnings("unchecked")
  private CacheEntry<K, V>[][] entries = new CacheEntry[4][0];

  public boolean has(EventType type) {
    return entries[type.ordinal()].length > 0;
  }


  /**
   * Fire events.
   * 
   * @param events a collection of events
   * @param type a type of event
   */
  public void fire(Iterable<CacheEntryEvent<? extends K, ? extends V>> events, EventType type) {
    try {
      CacheEntry<K, V>[] all = entries[type.ordinal()];
      for (int i = 0; i < all.length; i++) {
        all[i].fire(events);
      }
    } catch (CacheEntryListenerException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new CacheEntryListenerException(ex);
    }
  }

  @Override
  public void close() {
    for (CacheEntry<K, V>[] all : entries) {
      for (CacheEntry<K, V> e : all) {
        Io.close(e);
      }
    }
  }

  /**
   * Add a listener.
   * 
   * @param configListener a listener
   */
  @SuppressWarnings("unchecked")
  public void add(CacheEntryListenerConfiguration<K, V> configListener) {
    CacheEntryEventFilter<K, V> filter = configListener.getCacheEntryEventFilterFactory() == null
        ? null
        : (CacheEntryEventFilter<K, V>) configListener.getCacheEntryEventFilterFactory().create();
    CacheEntryListener<K, V> listener =
        (CacheEntryListener<K, V>) configListener.getCacheEntryListenerFactory().create();
    if (listener instanceof CacheEntryCreatedListener) {
      entries[EventType.CREATED.ordinal()] =
          Collections2.addToArray(entries[EventType.CREATED.ordinal()],
              new CreatedCacheEntry<>(filter, configListener, listener));
    }
    if (listener instanceof CacheEntryExpiredListener) {
      entries[EventType.EXPIRED.ordinal()] =
          Collections2.addToArray(entries[EventType.EXPIRED.ordinal()],
              new ExpiredCacheEntry<>(filter, configListener, listener));
    }
    if (listener instanceof CacheEntryRemovedListener) {
      entries[EventType.REMOVED.ordinal()] =
          Collections2.addToArray(entries[EventType.REMOVED.ordinal()],
              new RemovedCacheEntry<>(filter, configListener, listener));
    }
    if (listener instanceof CacheEntryUpdatedListener) {
      entries[EventType.UPDATED.ordinal()] =
          Collections2.addToArray(entries[EventType.UPDATED.ordinal()],
              new UpdatedCacheEntry<>(filter, configListener, listener));
    }
  }

  /**
   * Remove a listener.
   * 
   * @param configListener a listener
   */
  public void remove(CacheEntryListenerConfiguration<K, V> configListener) {
    for (int i = 0; i < 4; i++) {
      CacheEntry<K, V>[] all = entries[i];
      for (int j = all.length - 1; j >= 0; j--) {
        if (all[j].config == configListener) {
          entries[i] = all = Collections2.removeToArray(all, all[j]);
        }
      }
    }
  }

  private abstract static class CacheEntry<K, V> implements Closeable {
    private CacheEntryEventFilter<K, V> filter;
    private CacheEntryListenerConfiguration<K, V> config;

    public CacheEntry(CacheEntryEventFilter<K, V> filter,
        CacheEntryListenerConfiguration<K, V> config) {
      this.filter = filter;
      this.config = config;
    }

    @Override
    public void close() throws IOException {
      if (filter instanceof Closeable) {
        Io.close((Closeable) filter);
      }
    }

    abstract void fireEvent(Iterable<CacheEntryEvent<? extends K, ? extends V>> events);

    public void fire(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) {
      if (filter != null) {
        List<CacheEntryEvent<? extends K, ? extends V>> filtered = new ArrayList<>();
        for (CacheEntryEvent<? extends K, ? extends V> e : events) {
          if (filter.evaluate(e)) {
            filtered.add(e);
          }
        }
        fireEvent(filtered);
      } else {
        fireEvent(events);
      }
    }
  }

  private static class CreatedCacheEntry<K, V> extends CacheEntry<K, V> {

    private CacheEntryCreatedListener<K, V> listener;

    public CreatedCacheEntry(CacheEntryEventFilter<K, V> filter,
        CacheEntryListenerConfiguration<K, V> config, CacheEntryListener<K, V> listener) {
      super(filter, config);
      this.listener = (CacheEntryCreatedListener<K, V>) listener;
    }

    @Override
    public void close() throws IOException {
      super.close();
      if (listener instanceof Closeable) {
        Io.close((Closeable) listener);
      }
    }

    @Override
    void fireEvent(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) {
      listener.onCreated(events);
    }

  }

  private static class ExpiredCacheEntry<K, V> extends CacheEntry<K, V> {

    private CacheEntryExpiredListener<K, V> listener;

    public ExpiredCacheEntry(CacheEntryEventFilter<K, V> filter,
        CacheEntryListenerConfiguration<K, V> config, CacheEntryListener<K, V> listener) {
      super(filter, config);
      this.listener = (CacheEntryExpiredListener<K, V>) listener;
    }

    @Override
    public void close() throws IOException {
      super.close();
      if (listener instanceof Closeable) {
        Io.close((Closeable) listener);
      }
    }

    @Override
    void fireEvent(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) {
      listener.onExpired(events);
    }

  }

  private static class RemovedCacheEntry<K, V> extends CacheEntry<K, V> {

    private CacheEntryRemovedListener<K, V> listener;

    public RemovedCacheEntry(CacheEntryEventFilter<K, V> filter,
        CacheEntryListenerConfiguration<K, V> config, CacheEntryListener<K, V> listener) {
      super(filter, config);
      this.listener = (CacheEntryRemovedListener<K, V>) listener;
    }

    @Override
    public void close() throws IOException {
      super.close();
      if (listener instanceof Closeable) {
        Io.close((Closeable) listener);
      }
    }

    @Override
    void fireEvent(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) {
      listener.onRemoved(events);
    }

  }

  private static class UpdatedCacheEntry<K, V> extends CacheEntry<K, V> {

    private CacheEntryUpdatedListener<K, V> listener;

    public UpdatedCacheEntry(CacheEntryEventFilter<K, V> filter,
        CacheEntryListenerConfiguration<K, V> config, CacheEntryListener<K, V> listener) {
      super(filter, config);
      this.listener = (CacheEntryUpdatedListener<K, V>) listener;
    }

    @Override
    public void close() throws IOException {
      super.close();
      if (listener instanceof Closeable) {
        Io.close((Closeable) listener);
      }
    }

    @Override
    void fireEvent(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) {
      listener.onUpdated(events);
    }

  }

}
