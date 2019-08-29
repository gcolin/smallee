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

package net.gcolin.json;

import net.gcolin.common.lang.Strings;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

/**
 * The {@code JsonGeneratorImpl} class is a JsonObject.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonObjectImpl extends AbstractMap<String, JsonValue> implements JsonObject {

	private Map<String, JsonValue> delegate;
	private Set<Entry<String, JsonValue>> entries;

	public JsonObjectImpl(Map<String, JsonValue> delegate) {
		this.delegate = delegate;
	}

	public Map<String, JsonValue> getDelegate() {
		return delegate;
	}

	@Override
	public JsonValue get(Object key) {
		return delegate.get(key);
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public ValueType getValueType() {
		return ValueType.OBJECT;
	}

	@Override
	public JsonArray getJsonArray(String key) {
		return (JsonArray) get(key);
	}

	@Override
	public JsonObject getJsonObject(String key) {
		return (JsonObject) get(key);
	}

	@Override
	public JsonNumber getJsonNumber(String key) {
		return (JsonNumber) get(key);
	}

	@Override
	public JsonString getJsonString(String key) {
		return (JsonString) get(key);
	}

	@Override
	public String getString(String key) {
		return getJsonString(key).getString();
	}

	@Override
	public String getString(String key, String defaultValue) {
		JsonValue val = get(key);
		if (val != null && val instanceof JsonString) {
			return ((JsonString) val).getString();
		}
		return defaultValue;
	}

	@Override
	public int getInt(String key) {
		return getJsonNumber(key).intValue();
	}

	@Override
	public int getInt(String key, int defaultValue) {
		JsonValue val = get(key);
		if (val != null && val instanceof JsonNumber) {
			return ((JsonNumber) val).intValue();
		}
		return defaultValue;
	}

	@Override
	public boolean getBoolean(String key) {
		JsonValue val = get(key);
		if (val == TRUE) {
			return true;
		} else if (val == FALSE) {
			return false;
		} else {
			throw new ClassCastException("the element is not a boolean");
		}
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		JsonValue val = get(key);
		if (val == TRUE) {
			return true;
		} else if (val == FALSE) {
			return false;
		} else {
			return defaultValue;
		}
	}

	@Override
	public boolean isNull(String key) {
		return get(key) == NULL;
	}

	@Override
	public Set<Entry<String, JsonValue>> entrySet() {
		if (entries == null) {
			entries = Collections.unmodifiableSet(delegate.entrySet());
		}
		return entries;
	}	

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("{");
		for (Entry<String, JsonValue> e : entrySet()) {
			if (str.length() > 1) {
				str.append(',');
			}
			str.append('"');
			str.append(Strings.encodeJson(e.getKey()));
			str.append("\":");
			str.append(e.getValue().toString());
		}
		str.append('}');
		return str.toString();
	}

}
