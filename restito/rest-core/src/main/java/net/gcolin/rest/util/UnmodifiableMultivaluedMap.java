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

import net.gcolin.common.collection.UnmodifiableMap;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

/**
 * A MultivaluedMap unmodifiable.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class UnmodifiableMultivaluedMap<K, V> extends UnmodifiableMap<K, List<V>> implements MultivaluedMap<K, V> {

	private static final long serialVersionUID = 6179840439849509758L;
	private MultivaluedMap<K, V> map;

	public UnmodifiableMultivaluedMap(MultivaluedMap<K, V> map) {
		super(map);
		this.map = map;
	}

	@Override
	public void putSingle(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V getFirst(K key) {
		return map.getFirst(key);
	}

	@Override
	public void addAll(K key, @SuppressWarnings("unchecked") V... newValues) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addAll(K key, List<V> valueList) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addFirst(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equalsIgnoreValueOrder(MultivaluedMap<K, V> otherMap) {
		return map.equalsIgnoreValueOrder(otherMap);
	}

}
