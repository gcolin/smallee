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

import java.util.AbstractList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * A fast queue implementation FILO (First in Last out) based on an array. Also a fast List
 * implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @param <E> The queue item type
 */
@SuppressWarnings({"unchecked"})
public class ArrayQueue<E> extends AbstractList<E> implements Queue<E> {

  private int size;
  private Object[] data = new Object[8];

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public void clear() {
    if (size > 0) {
      for (int i = 0; i < size; i++) {
        data[i] = null;
      }
      size = 0;
    }
  }

  @Override
  public Iterator<E> iterator() {
    return new ArrayIterator();
  }

  @Override
  public E remove(int index) {
    final E element = (E) data[index];
    size--;
    if (index != size) {
      System.arraycopy(data, index + 1, data, index, size - index);
    }
    data[size] = null;
    return element;
  }
  
  @Override
  public E remove() {
    if (isEmpty()) {
      throw new EmptyStackException();
    }
    E element = (E) data[--size];
    data[size] = null;
    return element;
  }

  @Override
  public boolean add(E element) {
    return offer(element);
  }

  @Override
  public boolean offer(E element) {
    if (size == data.length) {
      Object[] tmp = new Object[data.length * 2];
      System.arraycopy(data, 0, tmp, 0, size);
      data = tmp;
    }
    data[size++] = element;
    return true;
  }

  @Override
  public E poll() {
    if (isEmpty()) {
      return null;
    }
    E element = (E) data[--size];
    data[size] = null;
    return element;
  }

  @Override
  public E element() {
    if (isEmpty()) {
      throw new EmptyStackException();
    }
    return (E) data[size - 1];
  }

  @Override
  public E peek() {
    if (isEmpty()) {
      return null;
    }
    return (E) data[size - 1];
  }

  @Override
  public E get(int index) {
    return (E) data[index];
  }

  private class ArrayIterator implements Iterator<E> {

    int index = size - 1;

    @Override
    public boolean hasNext() {
      return index >= 0;
    }

    @Override
    public E next() {
      if (hasNext()) {
        return (E) data[index--];
      }
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      ArrayQueue.this.remove(index + 1);
    }

  }
}
