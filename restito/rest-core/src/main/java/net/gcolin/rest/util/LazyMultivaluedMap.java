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

package net.gcolin.rest.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.ws.rs.core.MultivaluedMap;

/**
 * A MultivaluedMap loaded on the first real use.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LazyMultivaluedMap<K, V> implements MultivaluedMap<K, V> {

  private Supplier<MultivaluedMap<K, V>> delegateBuidler;
  private MultivaluedMap<K, V> delegate;

  public LazyMultivaluedMap(Supplier<MultivaluedMap<K, V>> delegateBuidler) {
    super();
    this.delegateBuidler = delegateBuidler;
  }

  private MultivaluedMap<K, V> get() {
    if (delegate == null) {
      delegate = delegateBuidler.get();
    }
    return delegate;
  }

  @Override
  public List<V> get(Object key) {
    return get().get(key);
  }

  @Override
  public int size() {
    return get().size();
  }

  @Override
  public boolean isEmpty() {
    return get().isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return get().containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return get().containsValue(value);
  }

  @Override
  public List<V> put(K key, List<V> value) {
    return get().put(key, value);
  }

  @Override
  public List<V> remove(Object key) {
    return get().remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends List<V>> map) {
    get().putAll(map);
  }

  @Override
  public void clear() {
    get().clear();
  }

  @Override
  public Set<K> keySet() {
    return get().keySet();
  }

  @Override
  public Collection<List<V>> values() {
    return get().values();
  }

  @Override
  public Set<Entry<K, List<V>>> entrySet() {
    return get().entrySet();
  }

  @Override
  public void add(K arg0, V arg1) {
    get().add(arg0, arg1);
  }

  @Override
  public void addAll(K arg0, @SuppressWarnings("unchecked") V... arg1) {
    get().addAll(arg0, arg1);
  }

  @Override
  public void addAll(K arg0, List<V> arg1) {
    get().addAll(arg0, arg1);
  }

  @Override
  public void addFirst(K arg0, V arg1) {
    get().addFirst(arg0, arg1);
  }

  @Override
  public boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> arg0) {
    return get().equalsIgnoreValueOrder(arg0);
  }

  @Override
  public V getFirst(K arg0) {
    return get().getFirst(arg0);
  }

  @Override
  public void putSingle(K arg0, V arg1) {
    get().putSingle(arg0, arg1);
  }

}
