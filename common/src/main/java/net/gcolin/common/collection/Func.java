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

package net.gcolin.common.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Some functional operation on collections.
 * 
 * @since 1.0
 * @author Gaël COLIN
 *
 */
public final class Func {

  private Func() {}

  /**
   * Find the first element which is accepted by the predicate.
   * 
   * @param <T> the type of the values
   * @param collection the collection
   * @param predicate the predicate
   * @return an element or null if no element matches the predicate
   */
  public static <T> T find(Collection<T> collection, Predicate<T> predicate) {
    if (collection == null || collection.isEmpty()) {
      return null;
    } else {
      for (T t : collection) {
        if (predicate.test(t)) {
          return t;
        }
      }
      return null;
    }
  }

  /**
   * Get the first element of the list.
   * 
   * @param <T> the type of the values
   * @param list a list
   * @return the first element or null if the list is null or empty
   */
  public static <T> T first(List<T> list) {
    if (list == null || list.isEmpty()) {
      return null;
    } else {
      return list.get(0);
    }
  }

  /**
   * Get the first element of the set.
   * 
   * @param <T> the type of the values
   * @param set a set
   * @return the first element or null if the list is null or empty
   */
  public static <T> T first(Set<T> set) {
    if (set == null || set.isEmpty()) {
      return null;
    } else {
      return set.iterator().next();
    }
  }

  /**
   * Get the second element of the list.
   * 
   * @param <T> the type of the values
   * @param list a list
   * @return the second element or null if the list is null or too small
   */
  public static <T> T second(List<T> list) {
    if (list == null || list.size() <= 1) {
      return null;
    } else {
      return list.get(1);
    }
  }

  /**
   * Get the last element of the list.
   * 
   * @param <T> the type of the values
   * @param list a list
   * @return the last element or null if the list is null or empty
   */
  public static <T> T last(List<T> list) {
    if (list == null || list.isEmpty()) {
      return null;
    } else {
      return list.get(list.size() - 1);
    }
  }

  /**
   * Transform an collection into a list with a function.
   * 
   * @param <T> the type of the input
   * @param <E> the type of the output
   * @param it an collection
   * @param transform a transform function to transform one element of the collection
   * @return a list with transformed elements
   */
  public static <T, E> List<E> map(Iterable<T> it, Function<T, E> transform) {
    List<E> list = new ArrayList<>();
    for (T e : it) {
      list.add(transform.apply(e));
    }
    return list;
  }

  /**
   * Transform an collection into a list with a function and filtered by a predicate.
   * 
   * @param <T> the type of the input
   * @param <E> the type of the output
   * @param it an collection
   * @param transform a transform function to transform one element of the collection
   * @param filter a filter predicate
   * @return a filtered list with transformed elements
   */
  public static <T, E> List<E> map(Iterable<T> it, Function<T, E> transform, Predicate<T> filter) {
    List<E> list = new ArrayList<>();
    for (T elt : it) {
      if (filter.test(elt)) {
        list.add(transform.apply(elt));
      }
    }
    return list;
  }

  /**
   * Transform a collection to a map.
   * 
   * @param <T> the type of the values
   * @param <K> the type of the keys
   * @param it a collection
   * @param keyGenerator a function to generate keys
   * @return a map
   */
  public static <T, K> Map<K, T> index(Iterable<T> it, Function<T, K> keyGenerator) {
    return index(it, keyGenerator, Function.identity());
  }

  /**
   * Transform a collection to a map.
   * 
   * @param <T> the type of the input
   * @param <V> the type of the values
   * @param <K> the type of the keys
   * @param it a collection
   * @param keyGenerator a function to generate keys
   * @param valueGenerator a function a generate values
   * @return a map
   */
  public static <T, K, V> Map<K, V> index(Iterable<T> it, Function<T, K> keyGenerator,
      Function<T, V> valueGenerator) {
    Map<K, V> indexA = new HashMap<>();
    if (it != null) {
      for (T oa : it) {
        indexA.put(keyGenerator.apply(oa), valueGenerator.apply(oa));
      }
    }
    return indexA;
  }

  /**
   * Transform a collection to a map of list values. If 2 elements have the same key, they are put
   * in the same value list.
   * 
   * @param <T> the type of the values
   * @param <K> the type of the keys
   * @param it a collection
   * @param keyGenerator a function to generate keys
   * @return a map
   */
  public static <T, K> Map<K, List<T>> indexList(Iterable<T> it, Function<T, K> keyGenerator) {
    return indexList(it, keyGenerator, null);
  }

  /**
   * Transform a collection to a map of sorted list values. If 2 elements have the same key, they
   * are put in the same value list.
   * 
   * @param <T> the type of the values
   * @param <K> the type of the keys
   * @param it a collection
   * @param keyGenerator a function to generate keys
   * @param sort a comparator to sort values
   * @return a map
   */
  public static <T, K> Map<K, List<T>> indexList(Iterable<T> it, Function<T, K> keyGenerator,
      Comparator<? super T> sort) {
    Map<K, List<T>> indexA = new HashMap<>();
    if (it != null) {
      for (T oa : it) {
        K key = keyGenerator.apply(oa);
        List<T> list = indexA.get(key);
        if (list == null) {
          list = new ArrayList<>();
          indexA.put(key, list);
        }
        list.add(oa);
        if (sort != null) {
          Collections.sort(list, sort);
        }
      }
    }
    return indexA;
  }

}
