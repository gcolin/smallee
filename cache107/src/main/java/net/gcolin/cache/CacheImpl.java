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

import net.gcolin.common.io.Io;
import net.gcolin.common.lang.Mutable;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;
import javax.cache.integration.CompletionListener;
import javax.cache.management.CacheStatisticsMXBean;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;

/**
 * the implementation of {@code Cache}
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 * @param <K> the type of key
 * @param <V> the type of value
 */
public class CacheImpl<K, V> extends ExpirableCache<CItem>
    implements
      Cache<K, V>,
      ExtCacheMxBean,
      CacheStatisticsMXBean {

  private Map<Object, CItem> map;
  private boolean closed;
  private CacheManagerImpl cacheManager;
  private String name;
  private MutableConfiguration<K, V> configuration;
  private CacheLoader<K, V> cacheLoader;
  private CacheWriter<K, V> cacheWriter;
  private InternalConverter<K> keyConverter;
  private InternalConverter<V> valueConverter;
  private ExpiryPolicy expiryPolicy;
  private LockManager<Object> lock = new LockManager<>();
  private EventManager<K, V> events = new EventManager<>();
  private final AtomicLong cacheRemovals = new AtomicLong();
  private final AtomicLong cachePuts = new AtomicLong();
  private final AtomicLong cacheHits = new AtomicLong();
  private final AtomicLong cacheMisses = new AtomicLong();
  private final AtomicLong cacheEvictions = new AtomicLong();
  private JmxHelper<K, V> jmx;

  @SuppressWarnings("unchecked")
  CacheImpl(CacheManagerImpl cacheManager, String cacheName, Configuration<K, V> configuration) {
    this.cacheManager = cacheManager;
    this.name = cacheName;

    // we make a copy of the configuration here so that the provided one
    // may be changed and or used independently for other caches. we do this
    // as we don't know if the provided configuration is mutable
    if (configuration instanceof CompleteConfiguration) {
      // support use of CompleteConfiguration
      this.configuration = new MutableConfiguration<>((CompleteConfiguration<K, V>) configuration);
    } else {
      // support use of Basic Configuration
      MutableConfiguration<K, V> mutableConfiguration = new MutableConfiguration<>();
      mutableConfiguration.setStoreByValue(configuration.isStoreByValue());
      mutableConfiguration.setTypes(configuration.getKeyType(), configuration.getValueType());
      this.configuration = mutableConfiguration;
    }

    if (this.configuration.getCacheLoaderFactory() != null) {
      cacheLoader = this.configuration.getCacheLoaderFactory().create();
    }
    if (this.configuration.getCacheWriterFactory() != null) {
      cacheWriter = (CacheWriter<K, V>) this.configuration.getCacheWriterFactory().create();
    }
    keyConverter = this.configuration.isStoreByValue()
        ? new SerializingInternalConverter<K>(cacheManager.getClassLoader())
        : new ReferenceInternalConverter<K>();

    valueConverter = this.configuration.isStoreByValue()
        ? new SerializingInternalConverter<V>(cacheManager.getClassLoader())
        : new ReferenceInternalConverter<V>();

    expiryPolicy = this.configuration.getExpiryPolicyFactory().create();

    map = new ConcurrentHashMap<>();

    for (CacheEntryListenerConfiguration<K, V> listenerConfiguration : this.configuration
        .getCacheEntryListenerConfigurations()) {
      events.add(listenerConfiguration);
    }

    jmx = new JmxHelper<>(this.configuration, this);

    if (this.configuration.isManagementEnabled()) {
      jmx.setManagementEnabled(true);
    }

    if (this.configuration.isStatisticsEnabled()) {
      jmx.setStatisticsEnabled(true);
    }
  }

  public JmxHelper<K, V> getJmx() {
    return jmx;
  }

  public int size() {
    cleanUp();
    return map.size();
  }

  private V load0(K key, Object internalKey) {
    V value;
    try {
      value = cacheLoader.load(key);
    } catch (CacheLoaderException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new CacheLoaderException("Exception in CacheLoader", ex);
    }
    if (value != null) {
      CItem item = new CItem();
      if (updateCreateTime(item)) {
        map.put(internalKey, item);
        item.node = evictionList.insert(item);
        cleanUp();
        item.value = valueConverter.toInternal(value);
      }
    }
    return value;
  }

  @Override
  public V get(K key) {
    checkOpen();
    checkKey(key);
    cleanUp();
    lock.lockRead(key);
    Object internalKey = keyConverter.toInternal(key);
    CItem item = map.get(internalKey);
    try {
      if (configuration.isStatisticsEnabled()) {
        if (item != null) {
          cacheHits.getAndIncrement();
        } else {
          cacheMisses.getAndIncrement();
        }
      }
      if (item == null && cacheLoader != null && configuration.isReadThrough()) {
        V resp = load0(key, internalKey);
        if (resp != null) {
          return resp;
        }
      }
    } finally {
      lock.unlockRead(key);
    }

    if (item == null) {
      return null;
    }
    V value = valueConverter.fromInternal(item.value);
    if (!updateAccessTime(item)) {
      remove0(key, value, internalKey, true, false);
    }
    return value;
  }

  private void checkKey(Object key) {
    if (key == null) {
      throw new NullPointerException("key is null");
    }
  }

  private void checkValue(Object value) {
    if (value == null) {
      throw new NullPointerException("value is null");
    }
  }

  private void checkOpen() {
    if (closed) {
      throw new IllegalStateException("cache is closed");
    }
  }

  @Override
  public Map<K, V> getAll(Set<? extends K> keys) {
    checkOpen();
    checkKey(keys);
    checkKeys(keys);
    Map<K, V> map = new HashMap<K, V>(keys.size());
    cleanUp();
    int get = 0;
    for (K key : keys) {
      CItem item;
      Object internalKey;
      lock.lockRead(key);
      try {
        internalKey = keyConverter.toInternal(key);
        item = this.map.get(internalKey);
        if (item != null) {
          get++;
        }
      } finally {
        lock.unlockRead(key);
      }
      if (item != null) {
        if (!updateAccessTime(item)) {
          remove0(key, null, internalKey, true, false);
        }
        map.put(key, valueConverter.fromInternal(item.value));
      } else if (cacheLoader != null && configuration.isReadThrough()) {
        V value = load0(key, internalKey);
        if (value != null) {
          map.put(key, value);
        }
      }
    }
    if (isStatisticsEnabled()) {
      cacheHits.getAndAdd(get);
      cacheMisses.getAndAdd((long) (get - map.size()));
    }
    return map;
  }

  private void checkKeys(Collection<?> keys) {
    if (keys.contains(null)) {
      throw new NullPointerException("keys contains null value");
    }
  }

  private void checkValues(Collection<?> values) {
    if (values.contains(null)) {
      throw new NullPointerException("values contains null value");
    }
  }

  @Override
  public boolean containsKey(K key) {
    checkOpen();
    checkKey(key);
    cleanUp();
    return map.containsKey(keyConverter.toInternal(key));
  }

  @Override
  public void loadAll(Set<? extends K> keys, boolean replaceExistingValues,
      CompletionListener completionListener) {
    checkOpen();
    if (cacheLoader == null) {
      if (completionListener != null) {
        completionListener.onCompletion();
      }
    } else {
      checkKey(keys);
      checkKeys(keys);
      cacheManager.getExecutorService()
          .submit(new AsyncLoad(keys, replaceExistingValues, completionListener));
    }
  }

  private class AsyncLoad implements Runnable {

    private Set<? extends K> keys;
    private boolean replaceExistingValues;
    private CompletionListener completionListener;

    public AsyncLoad(Set<? extends K> keys, boolean replaceExistingValues,
        CompletionListener completionListener) {
      this.keys = keys;
      this.replaceExistingValues = replaceExistingValues;
      this.completionListener = completionListener;
    }

    @Override
    public void run() {
      try {
        ArrayList<K> keysToLoad = new ArrayList<K>();
        for (K key : keys) {
          if (replaceExistingValues || !containsKey(key)) {
            keysToLoad.add(key);
          }
        }

        Map<? extends K, ? extends V> loaded;
        try {
          loaded = cacheLoader.loadAll(keysToLoad);
        } catch (Exception ex) {
          if (!(ex instanceof CacheLoaderException)) {
            throw new CacheLoaderException("Exception in CacheLoader", ex);
          } else {
            throw ex;
          }
        }

        for (K key : keysToLoad) {
          if (loaded.get(key) == null) {
            loaded.remove(key);
          }
        }

        putAll(loaded, false, false);

        if (completionListener != null) {
          completionListener.onCompletion();
        }
      } catch (Exception ex) {
        if (completionListener != null) {
          completionListener.onException(ex);
        }
      }
    }
  }

  @Override
  public void put(K key, V value) {
    checkOpen();
    checkKey(key);
    checkValue(value);
    cleanUp();
    lock.lockWrite(key);
    boolean put = false;
    try {
      Object internalKey = keyConverter.toInternal(key);
      CItem item = map.get(internalKey);
      if (item == null) {
        CItem newV = new CItem();
        if (updateCreateTime(newV)) {
          writeCacheEntry(key, value);
          newV.value = valueConverter.toInternal(value);
          newV.internalKey = internalKey;
          map.put(internalKey, newV);
          newV.node = evictionList.insert(newV);
          cleanUp();
          fireCreated(key, value);
          put = true;
        } else {
          fireExpired(key, value);
        }
      } else {
        if (updateUpdateTime(item)) {
          writeCacheEntry(key, value);
          V old = valueConverter.fromInternal(item.value);
          item.value = valueConverter.toInternal(value);
          item.node.update();
          fireUpdated(key, old, value);
          put = true;
        } else {
          remove0(key, value, internalKey, true, false);
        }
      }
      if (put && configuration.isStatisticsEnabled()) {
        cachePuts.getAndIncrement();
      }
    } finally {
      lock.unlockWrite(key);
    }
  }

  @Override
  public V getAndPut(K key, V value) {
    checkOpen();
    checkKey(key);
    checkValue(value);

    cleanUp();
    lock.lockWrite(key);
    try {
      Object internalKey = keyConverter.toInternal(key);
      CItem item = map.get(internalKey);
      V old = item == null ? null : valueConverter.fromInternal(item.value);

      if (old == null) {
        CItem newV = new CItem();
        if (updateCreateTime(newV)) {
          writeCacheEntry(key, value);
          newV.value = valueConverter.toInternal(value);
          map.put(internalKey, newV);
          newV.node = evictionList.insert(newV);
          cleanUp();
          fireCreated(key, value);
        } else {
          fireExpired(key, value);
        }
      } else {
        if (updateUpdateTime(item)) {
          writeCacheEntry(key, value);
          item.value = valueConverter.toInternal(value);
          item.node.update();
          fireUpdated(key, old, value);
        } else {
          remove0(key, value, internalKey, true, false);
        }
      }
      if (isStatisticsEnabled()) {
        cachePuts.getAndIncrement();
        if (old == null) {
          cacheMisses.getAndIncrement();
        } else {
          cacheHits.getAndIncrement();
        }
      }
      return old;
    } finally {
      lock.unlockWrite(key);
    }
  }

  boolean updateAccessTime(CItem item) {
    Duration duration = expiryPolicy.getExpiryForAccess();
    boolean updated = updateExpireTime(item, duration);
    if (updated) {
      item.node.update();
    }
    return updated;
  }

  private boolean updateExpireTime(CItem item, Duration duration) {
    if (duration != null) {
      if (duration.isZero()) {
        return false;
      } else if (duration.isEternal()) {
        item.expire = Long.MAX_VALUE;
      } else {
        item.expire = duration.getAdjustedTime(System.currentTimeMillis());
        if (nextTick == -1 || nextTick > item.expire) {
          nextTick = item.expire;
        }
      }
    }
    return true;
  }

  private boolean updateCreateTime(CItem item) {
    return updateExpireTime(item, expiryPolicy.getExpiryForCreation());
  }

  private boolean updateUpdateTime(CItem item) {
    return updateExpireTime(item, expiryPolicy.getExpiryForUpdate());
  }

  private void writeCacheEntry(K key, V value) {
    if (configuration.isWriteThrough() && cacheWriter != null) {
      try {
        cacheWriter.write(new EntryImpl<>(key, value));
      } catch (CacheWriterException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new CacheWriterException("Exception in CacheWriter", ex);
      }
    }
  }

  private void deleteCacheEntry(K key) {
    if (configuration.isWriteThrough() && cacheWriter != null) {
      try {
        cacheWriter.delete(key);
      } catch (CacheWriterException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new CacheWriterException("Exception in CacheWriter", ex);
      }
    }
  }

  private void fireUpdated(K key, V old, V value) {
    if (events.has(EventType.UPDATED)) {
      events.fire(
          Collections
              .singleton(new CacheEntryEventImpl<>(this, EventType.UPDATED, value, old, key)),
          EventType.UPDATED);
    }
  }

  private void fireCreated(K key, V value) {
    if (events.has(EventType.CREATED)) {
      events.fire(
          Collections
              .singleton(new CacheEntryEventImpl<>(this, EventType.CREATED, value, null, key)),
          EventType.CREATED);
    }
  }

  private void fireExpired(K key, V value) {
    if (events.has(EventType.EXPIRED)) {
      events.fire(
          Collections
              .singleton(new CacheEntryEventImpl<>(this, EventType.EXPIRED, value, null, key)),
          EventType.EXPIRED);
    }
  }

  private void putAll(Map<? extends K, ? extends V> map, boolean notquiet,
      boolean useWriteThrough) {
    checkOpen();
    checkValue(map);
    checkValues(map.values());
    checkKeys(map.keySet());
    cleanUp();
    boolean updateEvent = notquiet && events.has(EventType.UPDATED);
    boolean createdEvent = notquiet && events.has(EventType.CREATED);
    boolean expiredEvent = notquiet && events.has(EventType.EXPIRED);
    Collection<CacheEntryEvent<? extends K, ? extends V>> eventUpdateList = null;
    Collection<CacheEntryEvent<? extends K, ? extends V>> eventCreateList = null;
    Collection<CacheEntryEvent<? extends K, ? extends V>> eventExpireList = null;
    if (updateEvent) {
      eventUpdateList = new ArrayList<>();
    }
    if (createdEvent) {
      eventCreateList = new ArrayList<>();
    }
    if (expiredEvent) {
      eventExpireList = new ArrayList<>();
    }
    CacheWriterException exception = null;
    ArrayList<Cache.Entry<? extends K, ? extends V>> entriesToWrite =
        new ArrayList<Cache.Entry<? extends K, ? extends V>>();
    HashSet<K> keysToPut = new HashSet<K>();
    boolean isWriteThrough =
        configuration.isWriteThrough() && cacheWriter != null && useWriteThrough;

    for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {

      lock.lockWrite(e.getKey());

      keysToPut.add(e.getKey());

      if (isWriteThrough) {
        entriesToWrite.add(new EntryImpl<>(e.getKey(), e.getValue()));
      }
    }

    if (isWriteThrough) {
      try {
        cacheWriter.writeAll(entriesToWrite);
      } catch (CacheWriterException ex) {
        exception = ex;
      } catch (Exception ex) {
        exception = new CacheWriterException("Exception during write", ex);
      }

      for (Entry<?, ?> entry : entriesToWrite) {
        keysToPut.remove(entry.getKey());
      }
    }

    int puts = 0;
    for (K key : keysToPut) {
      V value = map.get(key);
      Object internalKey = keyConverter.toInternal(key);
      try {
        CItem item = this.map.get(internalKey);

        if (item == null) {
          CItem newV = new CItem();
          if (updateCreateTime(newV)) {
            newV.value = valueConverter.toInternal(value);
            this.map.put(internalKey, newV);
            newV.node = evictionList.insert(newV);
            puts++;
            if (createdEvent) {
              eventCreateList
                  .add(new CacheEntryEventImpl<K, V>(this, EventType.CREATED, value, null, key));
            }
          } else if (expiredEvent) {
            eventExpireList
                .add(new CacheEntryEventImpl<K, V>(this, EventType.EXPIRED, value, null, key));
          }
        } else {
          if (updateUpdateTime(item)) {
            if (updateEvent) {
              V old = valueConverter.fromInternal(item.value);
              eventUpdateList
                  .add(new CacheEntryEventImpl<K, V>(this, EventType.UPDATED, value, old, key));
            }
            item.value = valueConverter.toInternal(value);
            item.node.update();
            puts++;
          } else {
            remove0(key, value, internalKey, true, false);
            if (expiredEvent) {
              V old = valueConverter.fromInternal(item.value);
              eventExpireList
                  .add(new CacheEntryEventImpl<K, V>(this, EventType.EXPIRED, value, old, key));
            }
          }
        }
      } finally {
        lock.unlockWrite(key);
      }
    }
    cleanUp();
    if (notquiet && configuration.isStatisticsEnabled()) {
      cachePuts.getAndAdd(puts);
    }

    if (createdEvent && !eventCreateList.isEmpty()) {
      events.fire(eventCreateList, EventType.CREATED);
    }
    if (updateEvent && !eventUpdateList.isEmpty()) {
      events.fire(eventUpdateList, EventType.UPDATED);
    }
    if (expiredEvent && !eventExpireList.isEmpty()) {
      events.fire(eventExpireList, EventType.EXPIRED);
    }

    if (exception != null) {
      throw exception;
    }
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    putAll(map, true, true);
  }

  @Override
  public boolean putIfAbsent(K key, V value) {
    checkOpen();
    checkKey(key);
    checkValue(value);
    Object internalKey = keyConverter.toInternal(key);
    cleanUp();
    lock.lockWrite(key);
    try {
      if (map.containsKey(internalKey)) {
        return false;
      }
      CItem newV = new CItem();
      if (updateCreateTime(newV)) {
        newV.value = valueConverter.toInternal(value);
        map.put(internalKey, newV);
        newV.node = evictionList.insert(newV);
        cleanUp();
        writeCacheEntry(key, value);
        fireCreated(key, value);
        if (configuration.isStatisticsEnabled()) {
          cachePuts.getAndIncrement();
        }
      } else {
        fireExpired(key, value);
      }
      return true;
    } finally {
      lock.unlockWrite(key);
    }
  }

  private CItem remove0(K key, V value, Object internalKey, boolean evict, boolean updateHits) {
    lock.lockWrite(key);
    try {
      deleteCacheEntry(key);
      CItem item = map.remove(internalKey);
      if (item != null) {
        item.node.remove();
        if (evict) {
          if (events.has(EventType.EXPIRED)) {
            fireExpired(key, value == null ? valueConverter.fromInternal(item.value) : value);
          }
        } else {
          if (events.has(EventType.REMOVED)) {
            fireRemoved(key, value == null ? valueConverter.fromInternal(item.value) : value);
          }
        }
      }

      if (configuration.isStatisticsEnabled()) {
        if (item != null) {
          if (evict) {
            cacheEvictions.getAndIncrement();
          } else {
            cacheRemovals.getAndIncrement();
          }
          if (updateHits) {
            cacheHits.getAndIncrement();
          }
        } else if (updateHits) {
          cacheMisses.getAndIncrement();
        }
      }

      return item;
    } finally {
      lock.unlockWrite(key);
    }
  }

  private void fireRemoved(K key, V value) {
    if (events.has(EventType.REMOVED)) {
      events.fire(
          Collections
              .singleton(new CacheEntryEventImpl<>(this, EventType.REMOVED, value, null, key)),
          EventType.REMOVED);
    }
  }

  @Override
  public boolean remove(K key) {
    checkOpen();
    checkKey(key);
    cleanUp();
    return remove0(key, null, keyConverter.toInternal(key), false, false) != null;
  }

  @Override
  public boolean remove(K key, V oldValue) {
    checkOpen();
    checkKey(key);
    checkValue(oldValue);
    cleanUp();
    lock.lockWrite(key);
    Object internalKey = keyConverter.toInternal(key);
    try {
      CItem item = map.get(internalKey);
      if (item != null) {
        if (oldValue.equals(valueConverter.fromInternal(item.value))) {
          remove0(key, oldValue, internalKey, false, false);
          if (configuration.isStatisticsEnabled()) {
            cacheHits.getAndIncrement();
          }
          return true;
        } else if (!updateAccessTime(item)) {
          map.remove(internalKey);
        }
      } else if (configuration.isStatisticsEnabled()) {
        cacheMisses.getAndIncrement();
      }
      return false;
    } finally {
      lock.unlockWrite(key);
    }
  }

  @Override
  public V getAndRemove(K key) {
    checkOpen();
    checkKey(key);
    CItem item = remove0(key, null, keyConverter.toInternal(key), false, true);
    return item == null ? null : valueConverter.fromInternal(item.value);
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    checkOpen();
    checkKey(key);
    checkValue(oldValue);
    checkValue(newValue);
    cleanUp();
    lock.lockWrite(key);
    boolean result = false;
    Object internalKey = keyConverter.toInternal(key);
    try {
      CItem item = map.get(internalKey);
      if (item != null) {
        if (configuration.isStatisticsEnabled()) {
          cacheHits.getAndIncrement();
        }
        if (oldValue.equals(valueConverter.fromInternal(item.value))) {
          result = true;
          if (updateUpdateTime(item)) {
            item.value = valueConverter.toInternal(newValue);
            item.node.update();
          } else {
            map.remove(internalKey);
          }
          writeCacheEntry(key, newValue);
          fireUpdated(key, oldValue, newValue);
        } else if (!updateAccessTime(item)) {
          map.remove(internalKey);
        }
      } else if (configuration.isStatisticsEnabled()) {
        cacheMisses.getAndIncrement();
      }
    } finally {
      lock.unlockWrite(key);
    }
    if (configuration.isStatisticsEnabled() && result) {
      cachePuts.getAndIncrement();
    }
    return result;
  }

  @Override
  public boolean replace(K key, V value) {
    return replace0(key, value, true) != null;
  }

  private V replace0(K key, V newValue, boolean updateHit) {
    checkOpen();
    checkKey(key);
    checkValue(newValue);
    cleanUp();
    lock.lockWrite(key);
    V result = null;
    try {
      Object internalKey = keyConverter.toInternal(key);
      CItem item = map.get(internalKey);
      if (item != null) {
        result = valueConverter.fromInternal(item.value);
        if (updateUpdateTime(item)) {
          item.value = valueConverter.toInternal(newValue);
          item.node.update();
        } else {
          map.remove(internalKey);
        }
        writeCacheEntry(key, newValue);
        fireUpdated(key, result, newValue);
      }
    } finally {
      lock.unlockWrite(key);
    }
    if (configuration.isStatisticsEnabled()) {
      if (result != null) {
        cachePuts.getAndIncrement();
        if (updateHit) {
          cacheHits.getAndIncrement();
        }
      } else if (updateHit) {
        cacheMisses.getAndIncrement();
      }
    }
    return result;
  }

  @Override
  public V getAndReplace(K key, V value) {
    return replace0(key, value, true);
  }

  @Override
  public void removeAll(Set<? extends K> keys) {
    checkOpen();
    checkKey(keys);
    checkKeys(keys);
    cleanUp();
    boolean removeEvent = events.has(EventType.REMOVED);
    Collection<CacheEntryEvent<? extends K, ? extends V>> eventList = null;
    if (removeEvent) {
      eventList = new ArrayList<>();
    }
    for (K key : keys) {
      lock.lockWrite(key);
    }
    int remove = 0;
    CacheException exception = null;
    try {
      HashSet<K> cacheWriterKeys = new HashSet<K>(keys);
      boolean isWriteThrough = configuration.isWriteThrough() && cacheWriter != null;
      if (isWriteThrough) {
        try {
          cacheWriter.deleteAll(cacheWriterKeys);
        } catch (Exception ex) {
          if (!(ex instanceof CacheWriterException)) {
            exception = new CacheWriterException("Exception during write", ex);
          }
        }

        // At this point, cacheWriterKeys will contain only those that
        // were
        // _not_ written
        // Now delete only those that the writer deleted
        for (K key : keys) {
          // only delete those keys that the writer deleted. per
          // CacheWriter spec.
          if (!cacheWriterKeys.contains(key)) {
            Object internalKey = keyConverter.toInternal(key);
            CItem item = map.remove(internalKey);
            if (item != null) {
              remove++;
              if (removeEvent) {
                eventList.add(new CacheEntryEventImpl<>(this, EventType.REMOVED,
                    valueConverter.fromInternal(item.value), null, key));
              }
            }
          }
        }
      } else {
        for (K key : keys) {
          // only delete those keys that the writer deleted. per
          // CacheWriter spec.
          Object internalKey = keyConverter.toInternal(key);
          CItem item = map.remove(internalKey);
          if (item != null) {
            remove++;
            if (removeEvent) {
              eventList.add(new CacheEntryEventImpl<>(this, EventType.REMOVED,
                  valueConverter.fromInternal(item.value), null, key));
            }
          }
        }
      }

    } finally {
      for (K key : keys) {
        lock.unlockWrite(key);
      }
    }

    if (configuration.isStatisticsEnabled() && remove > 0) {
      cacheRemovals.getAndAdd(remove);
    }

    if (removeEvent && !eventList.isEmpty()) {
      events.fire(eventList, EventType.REMOVED);
    }

    if (exception != null) {
      throw exception;
    }
  }

  @Override
  public void removeAll() {
    checkOpen();
    if (map.isEmpty()) {
      return;
    }
    cleanUp();
    HashSet<K> keysToDelete = new HashSet<K>();
    for (Object k : map.keySet()) {
      keysToDelete.add(keyConverter.fromInternal(k));
    }
    removeAll(keysToDelete);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
    return clazz.isAssignableFrom(configuration.getClass()) ? (C) configuration : null;
  }

  void markAccess(K key, V value, CItem current, Object internalKey) {
    if (!updateAccessTime(current)) {
      remove0(key, null, internalKey, true, false);
    }
  }

  @Override
  public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments)
      throws EntryProcessorException {
    checkOpen();
    if (entryProcessor == null || key == null) {
      throw new NullPointerException();
    }
    cleanUp();
    Object internalKey = keyConverter.toInternal(key);
    CItem current = map.get(internalKey);

    if (configuration.isStatisticsEnabled()) {
      if (current != null) {
        cacheHits.getAndIncrement();
      } else {
        cacheMisses.getAndIncrement();
      }
    }

    MutableEntryImpl<K, V> entry = new MutableEntryImpl<>(this, key,
        current == null ? null : valueConverter.fromInternal(current.value), current, internalKey);
    T resp;
    try {
      resp = entryProcessor.process(entry, arguments);
      if (entry.getState() == MutableEntryImpl.STATE_REMOVE) {
        remove(entry.getKey());
      } else if (entry.getState() == MutableEntryImpl.STATE_CREATE) {
        put(entry.getKey(), entry.getValue());
      } else if (entry.getState() == MutableEntryImpl.STATE_UPDATE) {
        replace0(entry.getKey(), entry.getValue(), false);
      }
    } catch (EntryProcessorException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new EntryProcessorException(ex);
    }
    return resp;
  }

  @Override
  public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys,
      EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
    if (entryProcessor == null) {
      throw new NullPointerException();
    }
    Map<K, EntryProcessorResult<T>> map = new HashMap<>();
    for (K key : keys) {
      Mutable<EntryProcessorException> mex = new Mutable<>();
      Mutable<T> mut = new Mutable<>();
      try {
        mut.set(invoke(key, entryProcessor, arguments));
      } catch (EntryProcessorException ex) {
        mex.set(ex);
      } catch (Exception ex) {
        mex.set(new EntryProcessorException(ex));
      }
      if (mex.get() != null || mut.get() != null) {
        map.put(key, () -> {
          if (mex.get() == null) {
            return mut.get();
          } else {
            throw mex.get();
          }
        });
      }
    }
    return map;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public CacheManager getCacheManager() {
    return cacheManager;
  }

  @Override
  public void close() {
    if (!closed) {
      // ensure that any further access to this Cache will raise an
      // IllegalStateException
      closed = true;

      // ensure that the cache may no longer be accessed via the
      // CacheManager
      cacheManager.release(name);

      // disable statistics and management
      jmx.setStatisticsEnabled(false);
      jmx.setManagementEnabled(false);

      // close the configured CacheLoader
      if (cacheLoader instanceof Closeable) {
        Io.close((Closeable) cacheLoader);
      }

      // close the configured CacheWriter
      if (cacheWriter instanceof Closeable) {
        Io.close((Closeable) cacheWriter);
      }

      events.close();

      map.clear();
    }
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public <T> T unwrap(Class<T> clazz) {
    if (clazz.isAssignableFrom(getClass())) {
      return clazz.cast(this);
    }

    throw new IllegalArgumentException(
        "Unwapping to " + clazz + " is not a supported by this implementation");
  }

  @Override
  public void registerCacheEntryListener(
      CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
    events.add(cacheEntryListenerConfiguration);
    configuration.addCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
  }

  @Override
  public void deregisterCacheEntryListener(
      CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
    events.remove(cacheEntryListenerConfiguration);
    configuration.removeCacheEntryListenerConfiguration(cacheEntryListenerConfiguration);
  }

  @Override
  public Iterator<Entry<K, V>> iterator() {
    checkOpen();
    cleanUp();
    return new Iterator<Cache.Entry<K, V>>() {

      Map.Entry<Object, CItem> next;
      Map.Entry<Object, CItem> prec;
      K precKey;
      V precValue;
      Iterator<Map.Entry<Object, CItem>> it = map.entrySet().iterator();

      @Override
      public boolean hasNext() {
        if (it.hasNext() && next == null) {
          next = it.next();
          if (!updateAccessTime(next.getValue())) {
            CacheImpl.this.remove0(keyConverter.fromInternal(next.getKey()), null, next.getKey(),
                true, false);
          }
        }
        return next != null;
      }

      @Override
      public Entry<K, V> next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        Map.Entry<Object, CItem> me = next;
        prec = next;
        next = null;
        EntryImpl<K, V> entry = new EntryImpl<>(precKey = keyConverter.fromInternal(me.getKey()),
            precValue = valueConverter.fromInternal(me.getValue().value));
        if (configuration.isStatisticsEnabled()) {
          cacheHits.getAndIncrement();
        }
        return entry;
      }

      @Override
      public void remove() {
        if (prec == null) {
          throw new IllegalStateException("Must progress to the next entry to remove");
        } else {
          CacheImpl.this.remove0(precKey, precValue, prec.getKey(), false, false);
          prec = null;
        }
      }
    };
  }

  public Map<Object, CItem> getMap() {
    return map;
  }

  @Override
  public String getKeyType() {
    return configuration.getKeyType().getName();
  }

  @Override
  public String getValueType() {
    return configuration.getValueType().getName();
  }

  @Override
  public boolean isReadThrough() {
    return configuration.isReadThrough();
  }

  @Override
  public boolean isWriteThrough() {
    return configuration.isWriteThrough();
  }

  @Override
  public boolean isStoreByValue() {
    return configuration.isStoreByValue();
  }

  @Override
  public boolean isStatisticsEnabled() {
    return configuration.isStatisticsEnabled();
  }

  @Override
  public boolean isManagementEnabled() {
    return configuration.isManagementEnabled();
  }

  @Override
  public long getCacheHits() {
    return cacheHits.get();
  }

  @Override
  public float getCacheHitPercentage() {
    Long hits = getCacheHits();
    if (hits == 0) {
      return 0;
    }
    return (float) hits / getCacheGets() * 100.0f;
  }

  @Override
  public long getCacheMisses() {
    return cacheMisses.get();
  }

  @Override
  public float getCacheMissPercentage() {
    Long misses = getCacheMisses();
    if (misses == 0) {
      return 0;
    }
    return (float) misses / getCacheGets() * 100.0f;
  }

  @Override
  public long getCacheGets() {
    return getCacheHits() + getCacheMisses();
  }

  @Override
  public long getCachePuts() {
    return cachePuts.longValue();
  }

  @Override
  public long getCacheRemovals() {
    return cacheRemovals.longValue();
  }

  @Override
  public long getCacheEvictions() {
    return cacheEvictions.get();
  }

  @Override
  public float getAverageGetTime() {
    return 0;
  }

  @Override
  public float getAveragePutTime() {
    // should be between 0.01 and 0.001, this statistic is not calculated
    // because it add +80% time
    return 0;
  }

  @Override
  public float getAverageRemoveTime() {
    return 0;
  }

  @Override
  protected void evict(CItem item) {
    remove0(keyConverter.fromInternal(item.internalKey), null, item.internalKey, true, false);
  }

  @Override
  public int getLoaded() {
    cleanUp();
    return map.size();
  }


}
