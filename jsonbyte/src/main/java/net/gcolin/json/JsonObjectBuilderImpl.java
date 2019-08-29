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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * The {@code JsonGeneratorImpl} class is a JsonObjectBuilder.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonObjectBuilderImpl implements JsonObjectBuilder {

	/*
	 * a linkedhashmap for preserving order
	 */
	private Map<String, JsonValue> object = new LinkedHashMap<>();

	public JsonObjectBuilderImpl() {
	}
	
	public JsonObjectBuilderImpl(JsonObject obj) {
		object.putAll(obj);
	}

	@Override
	public JsonObjectBuilder add(String key, JsonValue val) {
		if (Strings.isNullOrEmpty(key)) {
			throw new NullPointerException();
		}
		object.put(key, val);
		return this;
	}

	@Override
	public JsonObjectBuilder add(String key, String val) {
		return add(key, new JsonStringImpl(val));
	}

	@Override
	public JsonObjectBuilder add(String key, BigInteger val) {
		return add(key, new BigIntegerJsonNumber(val));
	}

	@Override
	public JsonObjectBuilder add(String key, BigDecimal val) {
		return add(key, new BigDecimalJsonNumber(val));
	}

	@Override
	public JsonObjectBuilder add(String key, int val) {
		return add(key, new IntegerJsonNumber(val));
	}

	@Override
	public JsonObjectBuilder add(String key, long val) {
		return add(key, new LongJsonNumber(val));
	}

	@Override
	public JsonObjectBuilder add(String key, double val) {
		return add(key, new BigDecimal(String.valueOf(val)));
	}

	@Override
	public JsonObjectBuilder add(String key, boolean val) {
		return add(key, val ? JsonValue.TRUE : JsonValue.FALSE);
	}

	@Override
	public JsonObjectBuilder add(String key, JsonObjectBuilder val) {
		return add(key, val.build());
	}

	@Override
	public JsonObjectBuilder add(String key, JsonArrayBuilder val) {
		return add(key, val.build());
	}

	@Override
	public JsonObjectBuilder addNull(String key) {
		return add(key, JsonValue.NULL);
	}
	
	@Override
	public JsonObjectBuilder remove(String name) {
		object.remove(name);
		return this;
	}

	@Override
	public JsonObject build() {
		return new JsonObjectImpl(object);
	}

}
