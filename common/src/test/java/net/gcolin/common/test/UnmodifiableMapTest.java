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

import net.gcolin.common.collection.UnmodifiableMap;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class UnmodifiableMapTest {

  @Test
  public void simpleTest() {
    Map<String, String> map = new HashMap<>();
    map.put("hello", "world");
    Map<String, String> umap = new UnmodifiableMap<>(map);

    Assert.assertTrue(umap.containsKey("hello"));
    Assert.assertTrue(umap.containsValue("world"));
    Assert.assertNull(umap.get("HELLO"));
    Assert.assertEquals("world", umap.get("hello"));
    Assert.assertFalse(umap.isEmpty());
    Assert.assertEquals(1, umap.size());
    Assert.assertTrue(umap.keySet().contains("hello"));
    Assert.assertTrue(umap.entrySet().size() == 1);
    Assert.assertTrue(umap.entrySet() == umap.entrySet());
    Assert.assertTrue(umap.keySet() == umap.keySet());
    Assert.assertTrue(umap.values() == umap.values());
    Assert.assertTrue(umap.values().size() == 1);
    Assert.assertEquals(map.hashCode(), umap.hashCode());
    Assert.assertEquals(map.toString(), umap.toString());
    Assert.assertTrue(umap.equals(map));
    Assert.assertTrue(umap.equals(umap));

    try {
      umap.clear();
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.remove("");
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.put("", "");
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.compute(null, null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.computeIfAbsent(null, null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.computeIfPresent(null, null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.merge(null, null, null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.putAll(null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.putIfAbsent(null, null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.remove(null, null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.replace(null, null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.replace(null, null, null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }

    try {
      umap.replaceAll(null);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }
  }

}
