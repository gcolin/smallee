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

import net.gcolin.common.Logs;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * Add some utility method for manipulating collections
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 */
public class Collections2 {

  private Collections2() {}

  /**
   * Transform an iterator to a list.
   * 
   * @param <T> The type of the iterator items
   * @param it An iterator
   * @return The list from the iterator
   */
  public static <T> List<T> toList(Iterator<T> it) {
    List<T> list = new ArrayList<>();
    fill(it, list);
    return list;
  }
  
  /**
   * Transform an iterator to a list with a filter.
   * 
   * @param <T> The type of the iterator items
   * @param it An iterator
   * @param predicate A filter
   * @return The filtered list from the iterator
   */
  public static <T> List<T> toList(Iterator<T> it, Predicate<T> predicate) {
    List<T> list = new ArrayList<>();
    if (it != null) {
      while (it.hasNext()) {
        T elt = it.next();
        if (predicate.test(elt)) {
          list.add(elt);
        }
      }
    }
    return list;
  }
  
  /**
   * Convert an enumeration to a list.
   * 
   * @param <T> the collection element type
   * @param en an enumeration
   * @return a list
   */
  public static <T> List<T> toList(Enumeration<T> en) {
    List<T> list = new ArrayList<>();
    while (en.hasMoreElements()) {
      list.add(en.nextElement());
    }
    return list;
  }

  /**
   * Transform an iterator to a set.
   * 
   * @param <T> The type of the iterator items
   * @param it An iterator
   * @return The list from the iterator
   */
  public static <T> Set<T> toSet(Iterator<T> it) {
    Set<T> list = new HashSet<>();
    fill(it, list);
    return list;
  }
  
  /**
   * Create a set from an array of items.
   * 
   * @param <T> The type the items
   * @param array An array of items
   * @return A set
   */
  @SafeVarargs
  public static <T> Set<T> toSet(T... array) {
    Set<T> set = new HashSet<>(array.length);
    for (int i = 0; i < array.length; i++) {
      set.add(array[i]);
    }
    return set;
  }

  /**
   * Fill a collection with an iterator.
   * 
   * @param <T> The type of the iterator items
   * @param it An iterator
   * @param collection A collection
   */
  public static <T> void fill(Iterator<T> it, Collection<T> collection) {
    if (it != null) {
      while (it.hasNext()) {
        collection.add(it.next());
      }
    }
  }

  /**
   * Transform an iterator to an enumeration with a transformation.
   * 
   * @param <O> The type of the iterator items
   * @param <T> The type of the list items
   * @param it An iterator
   * @param transform A transformer from O to T
   * @return An enumeration
   */
  public static <O, T> Enumeration<T> toEnumeration(Iterator<O> it, Function<O, T> transform) {
    return new Enumeration<T>() {

      @Override
      public boolean hasMoreElements() {
        return it.hasNext();
      }

      @Override
      public T nextElement() {
        return transform.apply(it.next());
      }
    };
  }

  /**
   * Transform an iterator to an enumeration with a transformation and a filter.
   * 
   * @param <O> The type of the iterator items
   * @param <T> The type of the list items
   * @param it An iterator
   * @param transform A transformer from O to T
   * @param predicate A predicate
   * @return An enumeration
   */
  public static <O, T> Enumeration<T> toEnumeration(Iterator<O> it, Function<O, T> transform,
      Predicate<O> predicate) {
    return new CollectionEnumeration<>(it, transform, predicate);
  }

  /**
   * Load services in an array.
   * 
   * @param <T> the type of the services
   * @param cl The class loader for looking the services
   * @param type The service type
   * @param others Other services instances
   * @return An array of services
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] safeFillServiceLoaderAsArray(ClassLoader cl, Class<T> type, T... others) {
    List<T> list = new ArrayList<>();
    safeFillServiceLoader(cl, list, type);
    for (T other : others) {
      list.add(other);
    }
    Object array = Array.newInstance(type, list.size());
    for (int i = 0, l = list.size(); i < l; i++) {
      Array.set(array, i, list.get(i));
    }
    return (T[]) array;
  }

  /**
   * Load services in a collection.
   * 
   * @param <T> the type of the services
   * @param cl The class loader for looking the services
   * @param collection The collection for storing the services
   * @param type The service type
   */
  public static <T> void safeFillServiceLoader(ClassLoader cl, Collection<T> collection,
      Class<T> type) {
    Iterator<T> it = ServiceLoader.load(type, cl).iterator();
    while (it.hasNext()) {
      try {
        collection.add(it.next());
      } catch (ServiceConfigurationError ex) {
        Logs.LOG.info(ex.getMessage());
        Logs.LOG.log(Level.FINE, ex.getMessage(), ex);
      }
    }
  }

  /**
   * Remove an item form an array.
   * 
   * @param <T> The item types
   * @param array The array
   * @param element The item to remove
   * @return The array without the item.
   */
  public static <T> T[] removeToArray(T[] array, T element) {
    int index = -1;
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(element)) {
        index = i;
        break;
      }
    }
    if (index != -1) {
      return removeToArray(array, index);
    }
    return array;
  }

  /**
   * Remove an item form an array.
   * 
   * @param <T> The item types
   * @param array The array
   * @param index The item index to remove
   * @return The array without the item.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] removeToArray(T[] array, int index) {
    T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - 1);
    System.arraycopy(array, 0, newArray, 0, index);
    System.arraycopy(array, index + 1, newArray, index, array.length - 1 - index);
    return newArray;
  }

  /**
   * Search an item in an array.
   * 
   * @param <X> The predicate item type
   * @param <T> The item types
   * @param array The array
   * @param predicate The predicate
   * @return -1 or the index of the item.
   */
  public static <X, T extends X> int indexOf(T[] array, Predicate<X> predicate) {
    for (int i = 0; i < array.length; i++) {
      if (predicate.test(array[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Add a item to an array.
   * 
   * @param <T> The item types
   * @param array The array
   * @param element The item to add
   * @return the array with the item e
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] addToArray(T[] array, T element) {
    T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
    System.arraycopy(array, 0, newArray, 0, array.length);
    newArray[array.length] = element;
    return newArray;
  }

  /**
   * Merge 2 arrays.
   * 
   * @param <T> the collection element type
   * @param a1 array 1
   * @param a2 array 2
   * @return a merged array
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] merge(T[] a1, T[] a2) {
    if (a1 == null || a1.length == 0) {
      return a2;
    }
    if (a2 == null || a2.length == 0) {
      return a1;
    }
    T[] newArray = (T[]) Array.newInstance(a1.getClass().getComponentType(), a1.length + a2.length);
    System.arraycopy(a1, 0, newArray, 0, a1.length);
    System.arraycopy(a2, 0, newArray, a1.length, a2.length);
    return newArray;
  }

  private static class CollectionEnumeration<O, T> implements Enumeration<T> {

    private Iterator<O> it;
    private Function<O, T> transform;
    private Predicate<O> predicate;
    private O next = null;
    private boolean loaded;

    public CollectionEnumeration(Iterator<O> it, Function<O, T> transform, Predicate<O> predicate) {
      super();
      this.it = it;
      this.transform = transform;
      this.predicate = predicate;
    }

    @Override
    public boolean hasMoreElements() {
      while (!loaded && it.hasNext()) {
        next = it.next();
        loaded = true;
        if (!predicate.test(next)) {
          loaded = false;
        }
      }
      return loaded;
    }

    @Override
    public T nextElement() {
      hasMoreElements();
      if (!loaded) {
        throw new NoSuchElementException();
      }
      T obj = transform.apply(next);
      next = null;
      loaded = false;
      return obj;
    }

  }

  /**
   * Make a collection a set without checking the unicity of elements.
   * 
   * @param <E> the collection element type
   * @param collection collection to wrap
   * @return a set
   */
  public static <E> Set<E> wrapToSet(Collection<E> collection) {
    return new AbstractSet<E>() {

      @Override
      public int size() {
        return collection.size();
      }

      @Override
      public boolean isEmpty() {
        return collection.isEmpty();
      }

      @Override
      public boolean contains(Object obj) {
        return collection.contains(obj);
      }

      @Override
      public Iterator<E> iterator() {
        return collection.iterator();
      }

      @Override
      public Object[] toArray() {
        return collection.toArray();
      }

      @Override
      public <T> T[] toArray(T[] array) {
        return collection.toArray(array);
      }

      @Override
      public boolean add(E element) {
        return collection.add(element);
      }

      @Override
      public boolean remove(Object obj) {
        return collection.remove(obj);
      }

      @Override
      public void clear() {
        collection.clear();
      }

    };
  }
}
