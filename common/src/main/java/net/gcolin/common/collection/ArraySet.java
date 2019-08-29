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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * A set based on an array. Fast for iteration.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @param <E> the type of elements maintained by this set
 */
public class ArraySet<E> implements Set<E> {

  private E[] array;

  public ArraySet(E[] delegate) {
    array = delegate;
  }

  @Override
  public int size() {
    return array.length;
  }

  @Override
  public boolean isEmpty() {
    return array.length == 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean contains(Object obj) {
    for (int i = 0; i < array.length; i++) {
      if (equals((E) obj, array[i])) {
        return true;
      }
    }
    return false;
  }

  protected boolean equals(E obj, E other) {
    return Objects.equals(obj, other);
  }

  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {

      private int index = 0;

      @Override
      public boolean hasNext() {
        return index < array.length;
      }

      @Override
      public E next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return array[index++];
      }
    };
  }

  @Override
  public Object[] toArray() {
    return array;
  }

  @Override
  public <T> T[] toArray(T[] data) {
    System.arraycopy(array, 0, data, 0, Math.min(data.length, array.length));
    return data;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean add(E element) {
    if (contains(element)) {
      return false;
    }
    E[] tmp = (E[]) Array.newInstance(array.getClass().getComponentType(), array.length + 1);
    System.arraycopy(array, 0, tmp, 0, array.length);
    tmp[array.length] = element;
    array = tmp;
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean remove(Object obj) {
    if (!contains(obj)) {
      return false;
    }
    array = (E[]) Collections2.removeToArray(array, obj);
    return true;
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    for (Object obj : collection) {
      if (!contains(obj)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> collection) {
    boolean changed = false;
    for (E obj : collection) {
      changed |= add(obj);
    }
    return changed;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean retainAll(Collection<?> collection) {
    List<Object> newValues = new ArrayList<>();
    for (Object obj : collection) {
      if (contains(obj)) {
        newValues.add(obj);
      }
    }
    boolean changed = size() != newValues.size();
    E[] tmp = (E[]) Array.newInstance(array.getClass().getComponentType(), newValues.size());
    newValues.toArray(tmp);
    array = tmp;
    return changed;
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    boolean changed = false;
    for (Object obj : collection) {
      changed |= remove(obj);
    }
    return changed;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void clear() {
    array = (E[]) Array.newInstance(array.getClass().getComponentType(), 0);
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append('[');
    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        str.append(", ");
      }
      str.append(array[i]);
    }
    str.append(']');
    return str.toString();
  }

}
