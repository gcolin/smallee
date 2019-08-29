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

package net.gcolin.common.test;

import net.gcolin.common.collection.IgnoreCaseMap;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IgnoreCaseMapTest {

  @Test
  public void simpleTest() {
    Map<String, String> map = new HashMap<>();
    Map<String, String> impa = new IgnoreCaseMap<>(map);
    Assert.assertTrue(impa.isEmpty());
    Assert.assertEquals(0, impa.size());

    impa.put("Hello", "world");
    Assert.assertFalse(impa.isEmpty());
    Assert.assertEquals(1, impa.size());

    Assert.assertNull(impa.get(null));
    Assert.assertEquals("world", impa.get("Hello"));
    Assert.assertEquals("world", impa.get("hello"));
    Assert.assertEquals("world", impa.get("HELLO"));

    Assert.assertTrue(impa.containsKey("hello"));
    Assert.assertTrue(impa.containsValue("world"));

    Assert.assertEquals("world", impa.remove("HELLO"));
    Assert.assertTrue(impa.isEmpty());
    impa.putAll(Collections.singletonMap("Hello", "world"));

    Assert.assertTrue(impa.keySet().contains("hello"));
    Assert.assertTrue(impa.entrySet().size() == 1);
    Assert.assertTrue(impa.values().size() == 1);
    Assert.assertEquals(1, impa.size());
    Assert.assertEquals(map.hashCode(), impa.hashCode());
    Assert.assertEquals(map.toString(), impa.toString());
    Assert.assertTrue(impa.equals(map));
    Assert.assertTrue(impa.equals(impa));
    impa.clear();
    Assert.assertTrue(impa.isEmpty());
  }

}
