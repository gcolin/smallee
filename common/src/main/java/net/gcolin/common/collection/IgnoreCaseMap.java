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

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A Map&lt;String,?&gt; decorator which ignore case of the key.
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 * @param <V> the type of mapped values
 */
public class IgnoreCaseMap<V>  extends AbstractMap<String, V> {

  private Map<String, V> delegate;

  public IgnoreCaseMap(Map<String, V> delegate) {
    this.delegate = delegate;
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return delegate.containsKey(toKey((String) key));
  }

  @Override
  public boolean containsValue(Object value) {
    return delegate.containsValue(value);
  }

  @Override
  public V get(Object key) {
    return delegate.get(toKey((String) key));
  }

  @Override
  public V put(String key, V value) {
    return delegate.put(toKey(key), value);
  }

  @Override
  public V remove(Object key) {
    return delegate.remove(toKey((String) key));
  }

  @Override
  public void putAll(Map<? extends String, ? extends V> map) {
    for (Entry<? extends String, ? extends V> elt : map.entrySet()) {
      put(toKey(elt.getKey()), elt.getValue());
    }
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public Set<String> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<V> values() {
    return delegate.values();
  }

  @Override
  public Set<Entry<String, V>> entrySet() {
    return delegate.entrySet();
  }

  protected String toKey(String key) {
    return key == null ? null : key.toLowerCase();
  }

}
