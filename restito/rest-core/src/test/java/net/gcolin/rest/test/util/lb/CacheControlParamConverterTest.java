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

package net.gcolin.rest.test.util.lb;

import net.gcolin.rest.util.lb.CacheControlParamConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import javax.ws.rs.core.CacheControl;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CacheControlParamConverterTest {

  String privateCacheControl =
      "private=\"1,2,3\", no-cache=\"*.jsp\", no-store, no-transform, must-revalidate, "
          + "proxy-revalidate, max-age=10, s-maxage=11, other=1, other2=\"hello world\", "
          + "other3, other4=\"1,2\"";
  String privateCacheControl2 = "private, no-cache";
  String publicCacheControl = "public";
  CacheControl privCacheControl;
  CacheControl privCacheControl2;
  CacheControl pubCacheControl = new CacheControl();
  CacheControlParamConverter converter = new CacheControlParamConverter();

  /**
   * Initialize test.
   */
  @Before
  public void before() {
    privCacheControl = new CacheControl();
    privCacheControl.setPrivate(true);
    privCacheControl.getPrivateFields().addAll(Arrays.asList("1", "2", "3"));
    privCacheControl.setNoCache(true);
    privCacheControl.getNoCacheFields().addAll(Arrays.asList("*.jsp"));
    privCacheControl.setNoStore(true);
    privCacheControl.setNoTransform(true);
    privCacheControl.setMustRevalidate(true);
    privCacheControl.setProxyRevalidate(true);
    privCacheControl.setMaxAge(10);
    privCacheControl.setSMaxAge(11);
    privCacheControl.getCacheExtension().put("other", "1");
    privCacheControl.getCacheExtension().put("other2", "hello world");
    privCacheControl.getCacheExtension().put("other3", "");
    privCacheControl.getCacheExtension().put("other4", "1,2");

    privCacheControl2 = new CacheControl();
    privCacheControl2.setPrivate(true);
    privCacheControl2.setNoCache(true);
    privCacheControl2.setNoTransform(false);

    pubCacheControl.setNoTransform(false);
  }

  @Test
  public void fromStringTest() {
    Assert.assertEquals(privCacheControl, converter.fromString(privateCacheControl));
    Assert.assertEquals(privCacheControl2, converter.fromString(privateCacheControl2));
    Assert.assertEquals(pubCacheControl, converter.fromString(publicCacheControl));
  }

  @Test
  public void toStringTest() {
    Assert.assertEquals(privateCacheControl, converter.toString(privCacheControl));
    Assert.assertEquals(privateCacheControl2, converter.toString(privCacheControl2));
    Assert.assertEquals(publicCacheControl, converter.toString(pubCacheControl));
  }

}
