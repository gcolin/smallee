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

package net.gcolin.mustache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Partial Finder base.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class AbstractPartialFinder implements Map<String, String> {

	private Map<String, String> map = new ConcurrentHashMap<>();
	private String extension;

	/**
	 * Create a partial finder.
	 * 
	 * @param extension the extension of mustache file. by default the extension is
	 *                  ".mustache"
	 */
	public AbstractPartialFinder(String extension) {
		this.extension = extension;
		if (extension == null) {
			this.extension = ".mustache";
		}
		if (!this.extension.startsWith(".")) {
			this.extension = "." + this.extension;
		}
	}

	public String getExtension() {
		return extension;
	}

	protected abstract void load();

	@Override
	public int size() {
		load();
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		load();
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return get(key) != null;
	}

	@Override
	public boolean containsValue(Object value) {
		load();
		return map.containsValue(value);
	}

	@Override
	public String put(String key, String value) {
		return map.put(key, value);
	}

	@Override
	public String get(Object key) {
		return map.get(key);
	}

	@Override
	public String remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> om) {
		map.putAll(om);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public Set<String> keySet() {
		load();
		return map.keySet();
	}

	@Override
	public Collection<String> values() {
		load();
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, String>> entrySet() {
		load();
		return map.entrySet();
	}

}
