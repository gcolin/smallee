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

package cache.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

/**
 * An example.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class SpeedTest {

  /**
   * An example.
   * 
   * @param args args
   * @throws IOException if an IOException occurs
   * @throws URISyntaxException if an URISyntaxException occurs
   */
  public static void main(String[] args) throws IOException, URISyntaxException {

    MutableConfiguration<String, String> config = new MutableConfiguration<>();
    config.setTypes(String.class, String.class);
    config.setExpiryPolicyFactory(new FactoryBuilder.SingletonFactory<>(new ExpiryPolicy() {

      Duration dd = new Duration(TimeUnit.MINUTES, 30);

      @Override
      public Duration getExpiryForUpdate() {
        return dd;
      }

      @Override
      public Duration getExpiryForCreation() {
        return dd;
      }

      @Override
      public Duration getExpiryForAccess() {
        return dd;
      }
    }));
    config.setStoreByValue(false);
    config.setStatisticsEnabled(true);
    config.setManagementEnabled(true);
    Cache<String, String> cache = Caching.getCachingProvider()
        .getCacheManager(new URI("config.xml"), SpeedTest.class.getClassLoader())
        .getCache("hello", String.class, String.class);

    cache.put(String.valueOf(-1), String.valueOf(-1));

    Time.tick();
    for (int i = 0; i < 100000; i++) {
      cache.put(String.valueOf(i), String.valueOf(i));
    }
    Time.tock("jcache");
    Time.tick();
    for (int i = 0; i < 100000; i++) {
      cache.get("99999");
    }
    Time.tock("jcache");

    System.gc();
    Runtime rt = Runtime.getRuntime();
    long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1024;
    System.out.println("memory usage " + usedMb + " kB");

  }

}
