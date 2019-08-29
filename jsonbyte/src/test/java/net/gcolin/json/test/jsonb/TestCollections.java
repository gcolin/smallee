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

package net.gcolin.json.test.jsonb;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Collection tests.
 * 
 * @author Gaël COLIN
 */
public class TestCollections extends AbstractMultiCharsetTest {

  public static enum TestEnum {
    VAL1, VAL2
  }

  public static class Sub {
    String value;

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      Sub other = (Sub) obj;
      if (value == null) {
        if (other.value != null) {
          return false;
        }
      } else if (!value.equals(other.value)) {
        return false;
      }
      return true;
    }
  }

  public static class Obj {
    Collection<String> collection;
    Map<String, Sub> map;
    Set<Sub> set;
    HashSet<Integer> hashset;
    NavigableSet<String> navigableset;
    SortedSet<String> sortedSet;
    TreeSet<String> treeset;
    LinkedHashSet<Long> linkedHashset;
    HashMap<String, Sub> hashmap;
    NavigableMap<String, String> navigableMap;
    SortedMap<String, String> sortedMap;
    TreeMap<String, String> treeMap;
    LinkedHashMap<String, String> linkedHashMap;
    List<Integer> list;
    ArrayList<Integer> arrayList;
    LinkedList<Integer> linkedList;
    Deque<Integer> deque;
    ArrayDeque<Integer> arrayDeque;
    Queue<Integer> queue;
    PriorityQueue<Integer> priorityQueue;
    EnumSet<TestEnum> enumSet;
    EnumMap<TestEnum, Integer> enumMap;
  }

  @Test
  public void testCollection() {
    Obj obj = new Obj();
    obj.collection = Arrays.asList("hello", "world");
    Obj o2 = test0(Obj.class, obj, "{\"collection\":[\"hello\",\"world\"]}");
    Assert.assertEquals(obj.collection, o2.collection);
  }

  @Test
  public void testMap() {
    Obj obj = new Obj();
    Sub sub = new Sub();
    sub.value = "world";
    obj.map = Collections.singletonMap("hello", sub);
    Obj o2 = test0(Obj.class, obj, "{\"map\":{\"hello\":{\"value\":\"world\"}}}");
    Assert.assertEquals(obj.map, o2.map);
  }

  @Test
  public void testSet() {
    Obj obj = new Obj();
    Sub sub = new Sub();
    sub.value = "world";
    obj.set = Collections.singleton(sub);
    Obj o2 = test0(Obj.class, obj, "{\"set\":[{\"value\":\"world\"}]}");
    Assert.assertEquals(obj.set, o2.set);
  }

  @Test
  public void testNavigableSet() {
    Obj obj = new Obj();
    obj.navigableset = new TreeSet<>();
    obj.navigableset.add("world");
    obj.navigableset.add("hello");
    Obj o2 = test0(Obj.class, obj, "{\"navigableset\":[\"hello\",\"world\"]}");
    Assert.assertEquals(obj.navigableset, o2.navigableset);
  }

  @Test
  public void testSortedSet() {
    Obj obj = new Obj();
    obj.sortedSet = new TreeSet<>();
    obj.sortedSet.add("world");
    obj.sortedSet.add("hello");
    Obj o2 = test0(Obj.class, obj, "{\"sortedSet\":[\"hello\",\"world\"]}");
    Assert.assertEquals(obj.sortedSet, o2.sortedSet);
  }

  @Test
  public void testTreeSet() {
    Obj obj = new Obj();
    obj.treeset = new TreeSet<>();
    obj.treeset.add("world");
    obj.treeset.add("hello");
    Obj o2 = test0(Obj.class, obj, "{\"treeset\":[\"hello\",\"world\"]}");
    Assert.assertEquals(obj.treeset, o2.treeset);
  }

  @Test
  public void testHashSet() {
    Obj obj = new Obj();
    obj.hashset = new HashSet<>();
    obj.hashset.add(123);
    Obj o2 = test0(Obj.class, obj, "{\"hashset\":[123]}");
    Assert.assertEquals(obj.hashset, o2.hashset);
  }

  @Test
  public void testLinkedHashSet() {
    Obj obj = new Obj();
    obj.linkedHashset = new LinkedHashSet<>();
    obj.linkedHashset.add(123L);
    Obj o2 = test0(Obj.class, obj, "{\"linkedHashset\":[123]}");
    Assert.assertEquals(obj.linkedHashset, o2.linkedHashset);
  }

  @Test
  public void testHashMap() {
    Obj obj = new Obj();
    Sub sub = new Sub();
    sub.value = "world";
    obj.hashmap = new HashMap<>();
    obj.hashmap.put("hello", sub);
    Obj o2 = test0(Obj.class, obj, "{\"hashmap\":{\"hello\":{\"value\":\"world\"}}}");
    Assert.assertEquals(obj.hashmap, o2.hashmap);
  }

  @Test
  public void testNavigableMap() {
    Obj obj = new Obj();
    obj.navigableMap = new TreeMap<>();
    obj.navigableMap.put("world", "0");
    obj.navigableMap.put("hello", "1");
    Obj o2 = test0(Obj.class, obj, "{\"navigableMap\":{\"hello\":\"1\",\"world\":\"0\"}}");
    Assert.assertEquals(obj.navigableMap, o2.navigableMap);
  }

  @Test
  public void testsortedMap() {
    Obj obj = new Obj();
    obj.sortedMap = new TreeMap<>();
    obj.sortedMap.put("world", "0");
    obj.sortedMap.put("hello", "1");
    Obj o2 = test0(Obj.class, obj, "{\"sortedMap\":{\"hello\":\"1\",\"world\":\"0\"}}");
    Assert.assertEquals(obj.sortedMap, o2.sortedMap);
  }

  @Test
  public void testTreeMap() {
    Obj obj = new Obj();
    obj.treeMap = new TreeMap<>();
    obj.treeMap.put("world", "0");
    obj.treeMap.put("hello", "1");
    Obj o2 = test0(Obj.class, obj, "{\"treeMap\":{\"hello\":\"1\",\"world\":\"0\"}}");
    Assert.assertEquals(obj.treeMap, o2.treeMap);
  }

  @Test
  public void testLinkedHashMap() {
    Obj obj = new Obj();
    obj.linkedHashMap = new LinkedHashMap<>();
    obj.linkedHashMap.put("world", "0");
    obj.linkedHashMap.put("hello", "1");
    Obj o2 = test0(Obj.class, obj, "{\"linkedHashMap\":{\"world\":\"0\",\"hello\":\"1\"}}");
    Assert.assertEquals(obj.linkedHashMap, o2.linkedHashMap);
  }

  @Test
  public void testList() {
    Obj obj = new Obj();
    obj.list = Arrays.asList(1, 2, 3);
    Obj o2 = test0(Obj.class, obj, "{\"list\":[1,2,3]}");
    Assert.assertEquals(obj.list, o2.list);
  }

  @Test
  public void testArrayList() {
    Obj obj = new Obj();
    obj.arrayList = new ArrayList<>(Arrays.asList(1, 2, 3));
    Obj o2 = test0(Obj.class, obj, "{\"arrayList\":[1,2,3]}");
    Assert.assertEquals(obj.arrayList, o2.arrayList);
  }

  @Test
  public void testLinkedList() {
    Obj obj = new Obj();
    obj.linkedList = new LinkedList<>(Arrays.asList(1, 2, 3));
    Obj o2 = test0(Obj.class, obj, "{\"linkedList\":[1,2,3]}");
    Assert.assertEquals(obj.linkedList, o2.linkedList);
  }

  @Test
  public void testDeque() {
    Obj obj = new Obj();
    obj.deque = new ArrayDeque<>(Arrays.asList(1, 2, 3));
    Obj o2 = test0(Obj.class, obj, "{\"deque\":[1,2,3]}");
    Assert.assertEquals(obj.deque.size(), o2.deque.size());
    Assert.assertTrue(obj.deque.containsAll(o2.deque));
  }

  @Test
  public void testArrayDeque() {
    Obj obj = new Obj();
    obj.arrayDeque = new ArrayDeque<>(Arrays.asList(1, 2, 3));
    Obj o2 = test0(Obj.class, obj, "{\"arrayDeque\":[1,2,3]}");
    Assert.assertEquals(obj.arrayDeque.size(), o2.arrayDeque.size());
    Assert.assertTrue(obj.arrayDeque.containsAll(o2.arrayDeque));
  }

  @Test
  public void testQueue() {
    Obj obj = new Obj();
    obj.queue = new ArrayDeque<>(Arrays.asList(1, 2, 3));
    Obj o2 = test0(Obj.class, obj, "{\"queue\":[1,2,3]}");
    Assert.assertEquals(obj.queue.size(), o2.queue.size());
    Assert.assertTrue(obj.queue.containsAll(o2.queue));
  }

  @Test
  public void testPriorityQueue() {
    Obj obj = new Obj();
    obj.priorityQueue = new PriorityQueue<>(Arrays.asList(3, 1, 2));
    Obj o2 = test0(Obj.class, obj, "{\"priorityQueue\":[1,3,2]}");
    Assert.assertEquals(obj.priorityQueue.size(), o2.priorityQueue.size());
    Assert.assertTrue(obj.priorityQueue.containsAll(o2.priorityQueue));
  }

  @Test
  public void testEnumSet() {
    Obj obj = new Obj();
    obj.enumSet = EnumSet.of(TestEnum.VAL2);
    Obj o2 = test0(Obj.class, obj, "{\"enumSet\":[\"VAL2\"]}");
    Assert.assertEquals(obj.enumSet.size(), o2.enumSet.size());
    Assert.assertTrue(obj.enumSet.containsAll(o2.enumSet));
  }

  @Test
  public void testEnumMap() {
    Obj obj = new Obj();
    obj.enumMap = new EnumMap<>(TestEnum.class);
    obj.enumMap.put(TestEnum.VAL1, 1);
    Obj o2 = test0(Obj.class, obj, "{\"enumMap\":{\"VAL1\":1}}");
    Assert.assertEquals(obj.enumMap, o2.enumMap);
  }
}
