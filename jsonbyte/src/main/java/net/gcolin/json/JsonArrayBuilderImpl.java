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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * The {@code JsonArrayBuilderImpl} class represents a JsonArrayBuilder.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonArrayBuilderImpl implements JsonArrayBuilder {

	private List<JsonValue> all = new ArrayList<>();

	public JsonArrayBuilderImpl() {
	}

	@Override
	public JsonArrayBuilder add(JsonValue val) {
		if (val == null) {
			throw new NullPointerException();
		}
		all.add(val);
		return this;
	}

	@Override
	public JsonArrayBuilder add(String val) {
		return add(new JsonStringImpl(val));
	}

	@Override
	public JsonArrayBuilder add(BigDecimal val) {
		return add(new BigDecimalJsonNumber(val));
	}

	@Override
	public JsonArrayBuilder add(BigInteger val) {
		return add(new BigIntegerJsonNumber(val));
	}

	@Override
	public JsonArrayBuilder add(int val) {
		return add(new IntegerJsonNumber(val));
	}

	@Override
	public JsonArrayBuilder add(long val) {
		return add(new LongJsonNumber(val));
	}

	@Override
	public JsonArrayBuilder add(double val) {
		return add(new BigDecimal(String.valueOf(val)));
	}

	@Override
	public JsonArrayBuilder add(boolean val) {
		return add(val ? JsonValue.TRUE : JsonValue.FALSE);
	}

	@Override
	public JsonArrayBuilder add(JsonObjectBuilder val) {
		return add(val.build());
	}

	@Override
	public JsonArrayBuilder add(JsonArrayBuilder val) {
		return add(val.build());
	}

	@Override
	public JsonArrayBuilder addNull() {
		return add(JsonValue.NULL);
	}

	@Override
	public JsonArrayBuilder add(int index, JsonValue val) {
		if (val == null) {
			throw new NullPointerException();
		}
		all.add(index, val);
		return this;
	}

	@Override
	public JsonArrayBuilder add(int index, BigDecimal value) {
		return add(index, new BigDecimalJsonNumber(value));
	}

	@Override
	public JsonArrayBuilder add(int index, BigInteger value) {
		return add(index, new BigIntegerJsonNumber(value));
	}

	@Override
	public JsonArrayBuilder add(int index, boolean value) {
		return add(index, value ? JsonValue.TRUE : JsonValue.FALSE);
	}

	@Override
	public JsonArrayBuilder add(int index, double value) {
		return add(index, new BigDecimal(String.valueOf(value)));
	}

	@Override
	public JsonArrayBuilder add(int index, int value) {
		return add(index, new IntegerJsonNumber(value));
	}

	@Override
	public JsonArrayBuilder add(int index, JsonArrayBuilder builder) {
		return add(index, builder.build());
	}

	@Override
	public JsonArrayBuilder add(int index, JsonObjectBuilder builder) {
		return add(index, builder.build());
	}

	@Override
	public JsonArrayBuilder add(int index, long value) {
		return add(index, new LongJsonNumber(value));
	}

	@Override
	public JsonArrayBuilder add(int index, String value) {
		return add(index, new JsonStringImpl(value));
	}

	@Override
	public JsonArrayBuilder addAll(JsonArrayBuilder builder) {
		if (builder == null) {
			throw new NullPointerException();
		}
		all.addAll(builder.build());
		return this;
	}

	@Override
	public JsonArrayBuilder addNull(int index) {
		return add(index, JsonValue.NULL);
	}

	@Override
	public JsonArrayBuilder remove(int index) {
		all.remove(index);
		return this;
	}

	@Override
	public JsonArrayBuilder set(int index, JsonValue value) {
		if (value == null) {
			throw new NullPointerException();
		}
		all.set(index, value);
		return this;
	}

	@Override
	public JsonArrayBuilder set(int index, BigDecimal value) {
		return set(index, new BigDecimalJsonNumber(new BigDecimal(String.valueOf(value))));
	}

	@Override
	public JsonArrayBuilder set(int index, BigInteger value) {
		return set(index, new BigIntegerJsonNumber(value));
	}

	@Override
	public JsonArrayBuilder set(int index, boolean value) {
		return set(index, value ? JsonValue.TRUE : JsonValue.FALSE);
	}

	@Override
	public JsonArrayBuilder set(int index, double value) {
		return set(index, new BigDecimal(String.valueOf(value)));
	}

	@Override
	public JsonArrayBuilder set(int index, int value) {
		return set(index, new IntegerJsonNumber(value));
	}

	@Override
	public JsonArrayBuilder set(int index, JsonArrayBuilder builder) {
		return set(index, builder.build());
	}

	@Override
	public JsonArrayBuilder set(int index, JsonObjectBuilder builder) {
		return set(index, builder.build());
	}

	@Override
	public JsonArrayBuilder set(int index, long value) {
		return set(index, new LongJsonNumber(value));
	}

	@Override
	public JsonArrayBuilder set(int index, String value) {
		return set(index, new JsonStringImpl(value));
	}

	@Override
	public JsonArrayBuilder setNull(int index) {
		return set(index, JsonValue.NULL);
	}

	@Override
	public JsonArray build() {
		return new JsonArrayImpl(all.toArray(new JsonValue[all.size()]));
	}

}
