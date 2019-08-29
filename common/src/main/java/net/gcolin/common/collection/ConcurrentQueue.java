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
import java.util.AbstractCollection;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A concurrent queue implementation FILO (First in Last out) based on an array with a limited size
 * 
 * @since 1.0
 * @author Gaël COLIN
 *
 */
public class ConcurrentQueue<E> extends AbstractCollection<E> implements Queue<E> {

  private Object[] data;
  private int size;
  private ReentrantLock lock = new ReentrantLock();

  public ConcurrentQueue(int nb) {
    data = new Object[nb];
  }

  @Override
  public int size() {
    lock.lock();
    try {
      return size;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean contains(Object obj) {
    lock.lock();
    try {
      for (int i = 0; i < size; i++) {
        if (Objects.equals(data[i], obj)) {
          return true;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Iterator<E> iterator() {
    return new ArrayIterator();
  }

  @Override
  public Object[] toArray() {
    lock.lock();
    try {
      Object[] copy = new Object[size];
      System.arraycopy(data, 0, copy, 0, size);
      return copy;
    } finally {
      lock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T[] toArray(T[] array) {
    lock.lock();
    try {
      T[] tmp = array.length >= size
          ? array
          : (T[]) Array.newInstance(array.getClass().getComponentType(), size);
      System.arraycopy(data, 0, tmp, 0, size);
      if (tmp.length > size) {
        tmp[size] = null;
      }
      return tmp;
    } finally {
      lock.unlock();
    }
  }

  private void remove(int index) {
    lock.lock();
    try {
      if (index < size && index > 0) {
        size--;
        if (index < size) {
          System.arraycopy(data, index + 1, data, index, size - index);
        }
        data[size] = null;
      } else {
        throw new ArrayIndexOutOfBoundsException();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean remove(Object obj) {
    lock.lock();
    try {
      for (int i = 0; i < size; i++) {
        if (Objects.equals(data[i], obj)) {
          size--;
          if (i < size) {
            System.arraycopy(data, i + 1, data, i, size - i);
          }
          data[size] = null;
          return true;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public E remove() {
    lock.lock();
    try {
      if (size == 0) {
        throw new EmptyStackException();
      }
      E element = (E) data[--size];
      data[size] = null;
      return element;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void clear() {
    lock.lock();
    try {
      for (int i = 0; i < size; i++) {
        data[i] = null;
      }
      size = 0;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean add(E element) {
    lock.lock();
    try {
      if (size < data.length) {
        data[size++] = element;
        return true;
      } else {
        return false;
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean offer(E element) {
    return add(element);
  }

  @SuppressWarnings("unchecked")
  @Override
  public E poll() {
    lock.lock();
    try {
      if (size == 0) {
        return null;
      }
      E element = (E) data[--size];
      data[size] = null;
      return element;
    } finally {
      lock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public E element() {
    lock.lock();
    try {
      if (size == 0) {
        throw new EmptyStackException();
      }
      return (E) data[size - 1];
    } finally {
      lock.unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public E peek() {
    lock.lock();
    try {
      return size == 0 ? null : (E) data[size - 1];
    } finally {
      lock.unlock();
    }
  }

  private class ArrayIterator implements Iterator<E> {

    int index = size - 1;

    @Override
    public boolean hasNext() {
      return index >= 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public E next() {
      lock.lock();
      try {
        if (hasNext()) {
          return (E) data[index--];
        }
        throw new NoSuchElementException();
      } finally {
        lock.unlock();
      }
    }

    @Override
    public void remove() {
      ConcurrentQueue.this.remove(index + 1);
    }

  }

}
