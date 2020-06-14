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

package net.gcolin.cache;

import java.util.AbstractCollection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Concurrent double linked list sorted with a comparator. Improve the performance of eviction.
 * 
 * @author Gaël COLIN
 *
 * @param <E> element type
 */
public class ConcurrentSortedCollection<E> extends AbstractCollection<E> {

  private volatile Node tail;
  private volatile Node head;
  private AtomicInteger size = new AtomicInteger(0);
  private Comparator<E> comparator;
  private ReentrantLock lock = new ReentrantLock();

  public ConcurrentSortedCollection(Comparator<E> comparator) {
    this.comparator = comparator;
  }

  @Override
  public boolean isEmpty() {
    return tail == null;
  }

  public Node getTail() {
    return tail;
  }

  /**
   * Insert an element in the collection.
   * 
   * @param elt element
   * @return an inserted node
   */
  public Node insert(E elt) {
    lock.lock();
    try {
      Node newNode = new Node(elt);
      if (tail == null) {
        head = tail = newNode;
      } else {
        insertFromHead(head, newNode);
      }
      size.getAndIncrement();
      return newNode;
    } finally {
      lock.unlock();
    }
  }

  private void insertFromHead(Node from, Node newNode) {
    Node current = from;
    while (true) {
      if (comparator.compare(current.element, newNode.element) >= 0) {
        Node next = current.next;
        newNode.next = next;
        newNode.prec = current;
        current.next = newNode;
        if (next != null) {
          next.prec = newNode;
        }
        if (current == head) {
          head = newNode;
        }
        break;
      } else if (current.prec == null) {
        newNode.next = current;
        current.prec = newNode;
        if (current == tail) {
          tail = newNode;
        }
        break;
      } else {
        current = current.prec;
      }
    }
  }

  private void insertFromQueue(Node from, Node newNode) {
    Node current = from;
    while (true) {
      if (comparator.compare(current.element, newNode.element) <= 0) {
        Node prec = current.prec;
        newNode.next = current;
        newNode.prec = prec;
        current.prec = newNode;
        if (prec != null) {
          prec.next = newNode;
        }
        if (current == tail) {
          tail = newNode;
        }
        break;
      } else if (current.next == null) {
        newNode.prec = current;
        current.next = newNode;
        if (current == head) {
          head = newNode;
        }
        break;
      } else {
        current = current.next;
      }
    }
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public Iterator<E> iterator() {
    return new It();
  }

  private class It implements Iterator<E> {

    private Node current = tail;

    @Override
    public boolean hasNext() {
      return current != null;
    }

    @Override
    public E next() {
      if (current == null) {
        throw new NoSuchElementException();
      }
      Node node = current;
      current = node.next;
      return node.element;
    }

    @Override
    public void remove() {
      current.prec.remove();
    }
  }

  public class Node {
    public E element;
    public volatile Node next;
    public volatile Node prec;

    public Node(E element) {
      this.element = element;
    }

    /**
     * Replace the node in the collection if needed.
     */
    public void update() {
      if (next != null && comparator.compare(next.element, element) > 0) {
        lock.lock();
        try {
          Node nnext = next;
          detach();
          insertFromQueue(nnext, this);
        } finally {
          lock.unlock();
        }
      }
      if (prec != null && comparator.compare(prec.element, element) < 0) {
        lock.lock();
        try {
          Node nprec = prec;
          detach();
          insertFromHead(nprec, this);
        } finally {
          lock.unlock();
        }
      }
    }

    /**
     * Remove the node from the collection.
     */
    public void remove() {
      lock.lock();
      try {
        detach();
        size.getAndDecrement();
      } finally {
        lock.unlock();
      }
    }

    private void detach() {
      if (prec != null) {
        prec.next = next;
      }
      if (next != null) {
        next.prec = prec;
      }
      if (tail == this) {
        if (next != null) {
          tail = next;
        } else {
          tail = prec;
        }

      }
      if (head == this) {
        if (prec != null) {
          head = prec;
        } else {
          head = next;
        }
      }
      prec = null;
      next = null;
    }
  }

}
