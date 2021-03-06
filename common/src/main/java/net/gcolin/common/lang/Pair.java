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

package net.gcolin.common.lang;

import java.util.Map.Entry;
import java.util.Objects;

/**
 * The {@code Pair} class represents an Entry.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see Entry
 */
public class Pair<K, V> implements Entry<K, V> {

	private K key;
	private V value;

	public Pair() {
	}

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public static <K, V> Pair<K, V> of(K key, V value) {
		return new Pair<K, V>(key, value);
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		V old = this.value;
		this.value = value;
		return old;
	}

	public K getLeft() {
		return getKey();
	}

	public void setKey(K key) {
		this.key = key;
	}

	public void setLeft(K key) {
		setKey(key);
	}

	public V getRight() {
		return getValue();
	}

	public void setRight(V value) {
		setValue(value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		return Objects.equals(key, other.key) && Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return "Pair [key=" + key + ", value=" + value + "]";
	}

}
