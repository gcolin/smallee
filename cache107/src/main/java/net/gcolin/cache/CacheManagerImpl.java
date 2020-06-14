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

import net.gcolin.cache.ext.XmlConfig;
import net.gcolin.common.collection.ArraySet;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.spi.CachingProvider;

/**
 * the implementation of {@code CacheManager}.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CacheManagerImpl implements CacheManager {

  private CachingProviderImpl provider;
  private URI uri;
  private Properties properties;
  private ClassLoader classLoader;
  private boolean closed;
  private Map<String, CacheImpl<Object, Object>> caches = new ConcurrentHashMap<>();

  /**
   * Create a CacheManager
   * 
   * @param provider the provider
   * @param uri      the URI of the cache manager. Can link to an XML configuration file.
   * @param classLoader the class loader of the cache keys and values
   * @param properties  properties of the cache manager
   */
  public CacheManagerImpl(CachingProviderImpl provider, URI uri, ClassLoader classLoader,
      Properties properties) {
    this.provider = provider;
    this.uri = uri;
    this.properties = properties;
    this.classLoader = classLoader;

    String uriString = uri.toString();
    if (uriString.endsWith(".xml")) {
      XmlConfig.config(this);
    }
  }

  public ExecutorService getExecutorService() {
    return provider.getExecutorService();
  }

  @Override
  public CachingProvider getCachingProvider() {
    return provider;
  }

  @Override
  public URI getURI() {
    return uri;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public Properties getProperties() {
    return properties;
  }

  private void checkOpen() {
    if (closed) {
      throw new IllegalStateException("cache is closed");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(
      String cacheName, C configuration) throws IllegalArgumentException {
    checkOpen();
    if (caches.containsKey(cacheName)) {
      throw new CacheException("the cache already exists with name " + cacheName);
    }
    CacheImpl<K, V> cache = new CacheImpl<>(this, cacheName, configuration);
    caches.put(cacheName, (CacheImpl<Object, Object>) cache);
    return cache;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType, Class<V> valueType) {
    checkOpen();
    if (keyType == null || valueType == null) {
      throw new NullPointerException("keyType and valueType cannot be null");
    }
    Cache<K, V> cache = (Cache<K, V>) caches.get(cacheName);
    if (cache == null) {
      return null;
    }
    Configuration<K, V> config = cache.getConfiguration(Configuration.class);
    if (config.getKeyType() != keyType || config.getValueType() != valueType) {
      throw new ClassCastException("bad types");
    }
    return cache;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> Cache<K, V> getCache(String cacheName) {
    checkOpen();
    Cache<K, V> cache = (Cache<K, V>) caches.get(cacheName);
    if (cache == null) {
      return null;
    } else {
      Configuration<K, V> configuration = cache.getConfiguration(Configuration.class);
      if (configuration.getKeyType().equals(Object.class)
          && configuration.getValueType().equals(Object.class)) {
        return cache;
      } else {
        throw new IllegalArgumentException(
            "Cache " + cacheName + " was " + "defined with specific types Cache<"
                + configuration.getKeyType() + ", " + configuration.getValueType() + "> "
                + "in which case CacheManager.getCache(String, Class, Class) must be used");
      }
    }

  }

  @Override
  public Iterable<String> getCacheNames() {
    if (closed) {
      return Collections.emptyList();
    } else {
      Set<String> keys = caches.keySet();
      return new ArraySet<>(keys.toArray(new String[keys.size()]));
    }
  }

  @Override
  public void destroyCache(String cacheName) {
    checkOpen();
    Cache<Object, Object> cache = caches.get(cacheName);
    if (cache != null) {
      cache.clear();
      cache.close();
    }
  }

  @Override
  public void enableManagement(String cacheName, boolean enabled) {
    checkOpen();
    CacheImpl<Object, Object> cache = caches.get(cacheName);
    if (cache != null) {
      cache.getJmx().setManagementEnabled(enabled);
    } else {
      throw new IllegalStateException();
    }
  }

  @Override
  public void enableStatistics(String cacheName, boolean enabled) {
    checkOpen();
    CacheImpl<Object, Object> cache = caches.get(cacheName);
    if (cache != null) {
      cache.getJmx().setStatisticsEnabled(enabled);
    } else {
      throw new IllegalStateException();
    }
  }

  @Override
  public void close() {
    if (!isClosed()) {
      for (Cache<?, ?> cache : caches.values()) {
        cache.close();
      }
      provider.release(uri, classLoader);
      closed = true;
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

  public void release(String name) {
    caches.remove(name);
  }

}
