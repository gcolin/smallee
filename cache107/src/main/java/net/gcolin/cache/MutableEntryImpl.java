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

import javax.cache.processor.MutableEntry;

/**
 * the implementation of {@code MutableEntry}
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 * @param <K> the type of key
 * @param <V> the type of value
 */
class MutableEntryImpl<K, V> extends EntryImpl<K, V> implements MutableEntry<K, V> {

  public static final int STATE_NONE = 0;
  public static final int STATE_CREATE = 1;
  public static final int STATE_UPDATE = 2;
  public static final int STATE_REMOVE = 3;
  public static final int STATE_LOAD = 4;
  private int state = STATE_NONE;
  private K key;
  private V value;
  private CacheImpl<K, V> cache;
  private CItem item;
  private Object internalKey;

  public MutableEntryImpl(CacheImpl<K, V> cache, K key, V value, CItem item, Object internalKey) {
    super(key, null);
    this.cache = cache;
    this.key = key;
    this.value = value;
    if (value != null) {
      this.item = item;
      this.internalKey = internalKey;
      state = STATE_LOAD;
    }
  }

  public int getState() {
    return state;
  }

  @Override
  public V getValue() {
    if (state == STATE_NONE) {
      value = cache.get(key);
    } else if (item != null) {
      cache.markAccess(key, value, item, internalKey);
      item = null;
      internalKey = null;
    }
    return value;
  }

  @Override
  public boolean exists() {
    return value != null;
  }

  @Override
  public void remove() {
    state = state == STATE_CREATE ? STATE_NONE : STATE_REMOVE;
    this.value = null;
    item = null;
  }

  @Override
  public void setValue(V value) {
    if (value == null) {
      throw new NullPointerException();
    }
    state = this.value == null || state == STATE_CREATE ? STATE_CREATE : STATE_UPDATE;
    this.value = value;
    item = null;
  }

}
