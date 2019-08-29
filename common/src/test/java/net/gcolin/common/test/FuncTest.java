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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.gcolin.common.collection.Func;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public class FuncTest {

  public static class Module {
    private String name;
    private String path;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getPath() {
      return path;
    }

    public void setPath(String path) {
      this.path = path;
    }

  }

  @Test
  public void testFirst() {
    assertNull(Func.first((List<String>) null));
    assertNull(Func.first((Set<String>) null));
    assertNull(Func.first(new ArrayList<>()));
    assertNull(Func.first(new HashSet<>()));
    assertEquals(1, ((Integer) Func.first(Arrays.asList(1, 2, 3))).intValue());

    TreeSet<Integer> set = new TreeSet<>();
    set.add(1);
    set.add(2);
    set.add(3);
    assertEquals(1, ((Integer) Func.first(set)).intValue());
  }

  @Test
  public void testSecond() {
    assertNull(Func.second(null));
    assertNull(Func.second(new ArrayList<>()));
    assertNull(Func.second(Arrays.asList(1)));
    assertEquals(2, ((Integer) Func.second(Arrays.asList(1, 2))).intValue());
    assertEquals(2, ((Integer) Func.second(Arrays.asList(1, 2, 3))).intValue());
  }

  @Test
  public void testLast() {
    assertNull(Func.last(null));
    assertNull(Func.last(new ArrayList<>()));
    assertEquals(1, ((Integer) Func.last(Arrays.asList(1))).intValue());
    assertEquals(3, ((Integer) Func.last(Arrays.asList(1, 2, 3))).intValue());
  }

  @Test
  public void testFind() {
    assertNull(Func.find((List<String>) null, x -> x.startsWith("h")));
    assertNull(Func.find(new ArrayList<String>(), x -> x.startsWith("h")));
    assertEquals("hello", Func.find(Arrays.asList("1", "hello"), x -> x.startsWith("h")));
    assertNull(Func.find(Arrays.asList("1", "wello"), x -> x.startsWith("h")));
  }

  @Test
  public void testIndex() {
    assertTrue(Func.index(null, Function.identity()).isEmpty());

    Map<Long, String> map = Func.index(Arrays.asList("1", "2"), x -> Long.parseLong(x));
    assertEquals(2, map.size());
    assertEquals("1", map.get(1L));
    assertEquals("2", map.get(2L));

    map = Func.index(Arrays.asList("1", "2"), x -> Long.parseLong(x), x -> x + x);
    assertEquals(2, map.size());
    assertEquals("11", map.get(1L));
    assertEquals("22", map.get(2L));
  }

  @Test
  public void testIndexList() {
    assertTrue(Func.indexList(null, Function.identity()).isEmpty());

    Map<Integer, List<String>> map =
        Func.indexList(Arrays.asList("1", "2", "3", "5"), x -> Integer.parseInt(x) % 2);
    assertEquals(2, map.size());
    assertEquals(Arrays.asList("1", "3", "5"), map.get(1));
    assertEquals(Arrays.asList("2"), map.get(0));

    map = Func.indexList(Arrays.asList("5", "2", "3", "1"), x -> Integer.parseInt(x) % 2,
        (a1, a2) -> a1.compareTo(a2));
    assertEquals(2, map.size());
    assertEquals(Arrays.asList("1", "3", "5"), map.get(1));
    assertEquals(Arrays.asList("2"), map.get(0));
  }
}
