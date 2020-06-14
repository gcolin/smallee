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
import net.gcolin.common.reflect.Injector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the implementation of {@code CachingProvider}
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CachingProviderImpl implements CachingProvider {

  private static final Injector[] INJECTORS = Collections2
      .safeFillServiceLoaderAsArray(CachingProviderImpl.class.getClassLoader(), Injector.class);
  public static final Logger LOGGER = LoggerFactory.getLogger("net.gcolin.cache");
  private Map<ClassLoader, Map<URI, CacheManager>> cacheManagersByClassLoader = new WeakHashMap<>();
  private ExecutorService executorService;

  /**
   * Create a CachingProvider.
   */
  public CachingProviderImpl() {
    // try to get ExecutorService from an injection framework
    for (int i = 0; i < INJECTORS.length && executorService == null; i++) {
      executorService = INJECTORS[i].get(ThreadPoolExecutor.class);
    }
    if (executorService == null) {
      executorService = Executors.newFixedThreadPool(1, new ThreadFactory() {

        @Override
        public Thread newThread(Runnable run) {
          Thread tr = new Thread(run);
          tr.setName("cache-thread");
          return tr;
        }
      });
    }
  }

  public ExecutorService getExecutorService() {
    return executorService;
  }

  @Override
  public synchronized CacheManager getCacheManager(URI uri, ClassLoader classLoader,
      Properties properties) {
    URI managerUri = uri == null ? getDefaultURI() : uri;
    ClassLoader managerClassLoader = classLoader == null ? getDefaultClassLoader() : classLoader;
    Properties managerProperties = properties == null ? new Properties() : properties;

    Map<URI, CacheManager> cacheManagersByUri = cacheManagersByClassLoader.get(managerClassLoader);

    if (cacheManagersByUri == null) {
      cacheManagersByUri = new HashMap<>();
      cacheManagersByClassLoader.put(classLoader, cacheManagersByUri);
    }

    CacheManager cacheManager = cacheManagersByUri.get(managerUri);

    if (cacheManager == null) {
      cacheManager = new CacheManagerImpl(this, managerUri, managerClassLoader, managerProperties);

      cacheManagersByUri.put(managerUri, cacheManager);
    }

    return cacheManager;
  }
  
  @Override
  public CacheManager getCacheManager(URI uri, ClassLoader classLoader) {
    return getCacheManager(uri, classLoader, getDefaultProperties());
  }
  
  @Override
  public CacheManager getCacheManager() {
    return getCacheManager(getDefaultURI(), getDefaultClassLoader(), getDefaultProperties());
  }

  @Override
  public ClassLoader getDefaultClassLoader() {
    return getClass().getClassLoader();
  }

  @Override
  public URI getDefaultURI() {
    try {
      return new URI(this.getClass().getName());
    } catch (URISyntaxException ex) {
      throw new CacheException(
          "Failed to create the default URI for the javax.cache Reference Implementation", ex);
    }
  }

  @Override
  public Properties getDefaultProperties() {
    return null;
  }

  /**
   * Remove a cache manager by URI. For internal use only.
   * 
   * @param uri cache manager URI
   * @param classLoader the class loader of the cache manager
   */
  public synchronized void release(URI uri, ClassLoader classLoader) {
    Map<URI, CacheManager> map = cacheManagersByClassLoader.get(classLoader);
    if (map != null) {
      map.remove(uri);
    }
  }

  @Override
  public synchronized void close() {
    for (Map<URI, CacheManager> v : cacheManagersByClassLoader.values()) {
      for (CacheManager c : new ArrayList<>(v.values())) {
        c.close();
      }
      v.clear();
    }
    cacheManagersByClassLoader.clear();
  }

  @Override
  public synchronized void close(ClassLoader classLoader) {
    Map<URI, CacheManager> map = cacheManagersByClassLoader.remove(classLoader);
    if (map != null) {
      for (CacheManager cm : new ArrayList<>(map.values())) {
        cm.close();
      }
      map.clear();
    }
  }

  @Override
  public synchronized void close(URI uri, ClassLoader classLoader) {
    Map<URI, CacheManager> map = cacheManagersByClassLoader.get(classLoader);
    if (map != null) {
      CacheManager cm = map.remove(uri);
      if (cm != null) {
        cm.close();
      }
    }
  }

  @Override
  public boolean isSupported(OptionalFeature optionalFeature) {
    return optionalFeature == OptionalFeature.STORE_BY_REFERENCE;
  }

}
