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

/**
 * An helper for register and unregister MBeans.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
import java.lang.management.ManagementFactory;
import java.util.Set;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.management.CacheStatisticsMXBean;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

public class JmxHelper<K, V> {

  private static final String STATISTICS = "Statistics";
  private static final String CONFIGURATION = "Configuration";
  private boolean jmxRegistration;
  private boolean jmxStatRegistration;
  private MutableConfiguration<K, V> configuration;
  private Cache<K, V> cache;

  public JmxHelper(MutableConfiguration<K, V> configuration, Cache<K, V> cache) {
    this.configuration = configuration;
    this.cache = cache;
  }

  /**
   * Active or not the statistics.
   * 
   * @param active {@code true} to active
   */
  public void setStatisticsEnabled(boolean active) {
    if (jmxStatRegistration == active) {
      return;
    }
    jmxStatRegistration = active;
    if (active) {
      registerCacheObject(STATISTICS);
    } else {
      unregisterCacheObject(STATISTICS);
    }
    configuration.setStatisticsEnabled(active);
  }

  /**
   * Active or not the JMX management.
   * 
   * @param active {@code true} to active
   */
  public void setManagementEnabled(boolean active) {
    if (jmxRegistration == active) {
      return;
    }
    jmxRegistration = active;
    if (active) {
      registerCacheObject(CONFIGURATION);
    } else {
      unregisterCacheObject(CONFIGURATION);
    }
    configuration.setManagementEnabled(active);
  }

  private void registerCacheObject(String type) {
    // these can change during runtime, so always look it up
    ObjectName registeredObjectName = calculateObjectName(type);

    try {
      MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
      ObjectName objectName = calculateObjectName(type);
      Set<ObjectName> registeredObjectNames = beanServer.queryNames(objectName, null);
      if (registeredObjectNames.isEmpty()) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        StandardMBean bean = new StandardMBean(cache, (Class) (CONFIGURATION.equals(type)
            ? ExtCacheMxBean.class
            : CacheStatisticsMXBean.class));
        beanServer.registerMBean(bean, registeredObjectName);
      }
    } catch (Exception ex) {
      throw new CacheException("Error registering cache MXBeans for CacheManager "
          + registeredObjectName + " . Error was " + ex.getMessage(), ex);
    }
  }

  private void unregisterCacheObject(String type) {
    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    ObjectName objectName = calculateObjectName(type);

    // should just be one
    for (ObjectName registeredObjectName : beanServer.queryNames(objectName, null)) {
      try {
        beanServer.unregisterMBean(registeredObjectName);
      } catch (Exception ex) {
        throw new CacheException("Error unregistering object instance " + registeredObjectName
            + " . Error was " + ex.getMessage(), ex);
      }
    }
  }

  /**
   * Creates an object name using the scheme
   * "javax.cache:type=Cache&lt;Statistics|Configuration&gt;,CacheManager=&lt;
   * cacheManagerName&gt;,name=&lt;cacheName&gt;".
   */
  private ObjectName calculateObjectName(String objectNameType) {
    String cacheManagerName = mbeanSafe(cache.getCacheManager().getURI().toString());
    String cacheName = mbeanSafe(cache.getName());

    try {
      return new ObjectName("javax.cache:type=Cache" + objectNameType + ",CacheManager="
          + cacheManagerName + ",Cache=" + cacheName);
    } catch (MalformedObjectNameException ex) {
      throw new CacheException("Illegal ObjectName for Management Bean. " + "CacheManager=["
          + cacheManagerName + "], Cache=[" + cacheName + "]", ex);
    }
  }

  /**
   * Filter out invalid ObjectName characters from string.
   *
   * @param string input string
   * @return A valid JMX ObjectName attribute value.
   */
  private String mbeanSafe(String string) {
    return string == null ? "" : string.replaceAll(",|:|=|\n", ".");
  }

}
