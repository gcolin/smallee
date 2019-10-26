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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An immutable map decorator. Similar to Collections.unmodifiableMap but can be
 * extended.
 *
 * @author Gaël COLIN
 * @since 1.0
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class UnmodifiableMap<K, V> implements Map<K, V> {

	private final Map<K, V> delagate;
	private transient Set<K> keySet;
	private transient Set<Map.Entry<K, V>> entrySet;
	private transient Collection<V> values;

	public UnmodifiableMap(Map<K, V> delegate) {
		this.delagate = delegate;
	}

	@Override
	public int size() {
		return delagate.size();
	}

	@Override
	public boolean isEmpty() {
		return delagate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delagate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object val) {
		return delagate.containsValue(val);
	}

	@Override
	public V get(Object key) {
		return delagate.get(key);
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<K> keySet() {
		if (keySet == null) {
			keySet = Collections.unmodifiableSet(delagate.keySet());
		}
		return keySet;
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		if (entrySet == null) {
			entrySet = Collections.unmodifiableSet(delagate.entrySet());
		}
		return entrySet;
	}

	@Override
	public Collection<V> values() {
		if (values == null) {
			values = Collections.unmodifiableCollection(delagate.values());
		}
		return values;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || delagate.equals(obj);
	}

	@Override
	public int hashCode() {
		return delagate.hashCode();
	}

	@Override
	public String toString() {
		return delagate.toString();
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V replace(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		throw new UnsupportedOperationException();
	}

}
