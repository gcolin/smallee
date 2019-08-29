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

import net.gcolin.common.collection.IgnoreCaseMap;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

/**
 * A MultivaluedMap with case insensitive String key.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class IgnoreCaseMultivaluedMap<V> extends IgnoreCaseMap<List<V>>
    implements
      MultivaluedMap<String, V> {

  private MultivaluedMap<String, V> delegate;

  public IgnoreCaseMultivaluedMap(MultivaluedMap<String, V> delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  @Override
  public void putSingle(String key, V value) {
    delegate.putSingle(toKey(key), value);
  }

  @Override
  public void add(String key, V value) {
    delegate.add(toKey(key), value);
  }

  @Override
  public V getFirst(String key) {
    return delegate.getFirst(toKey(key));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void addAll(String key, V... newValues) {
    delegate.addAll(toKey(key), newValues);
  }

  @Override
  public void addAll(String key, List<V> valueList) {
    delegate.addAll(toKey(key), valueList);
  }

  @Override
  public void addFirst(String key, V value) {
    delegate.addFirst(toKey(key), value);
  }

  @Override
  public boolean equalsIgnoreValueOrder(MultivaluedMap<String, V> otherMap) {
    return delegate.equalsIgnoreValueOrder(otherMap);
  }

}
