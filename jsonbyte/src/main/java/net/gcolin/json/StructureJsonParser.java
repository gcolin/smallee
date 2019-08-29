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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;

import net.gcolin.common.collection.ArrayQueue;
import net.gcolin.common.lang.Pair;

/**
 * The {@code StructureJsonParser} class read a JsonArray o JsonObject with the stream api of
 * JsonParser.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class StructureJsonParser implements JsonParser, JsonLocation {

	private Pair<Event, JsonValue> current;
	private Scope scope;
	private static final Event[] CONVERT = new Event[JsonValue.ValueType.values().length];

	static {
		CONVERT[ValueType.FALSE.ordinal()] = Event.VALUE_FALSE;
		CONVERT[ValueType.TRUE.ordinal()] = Event.VALUE_TRUE;
		CONVERT[ValueType.NUMBER.ordinal()] = Event.VALUE_NUMBER;
		CONVERT[ValueType.STRING.ordinal()] = Event.VALUE_STRING;
		CONVERT[ValueType.NULL.ordinal()] = Event.VALUE_NULL;
	}

	public StructureJsonParser(JsonArray array) {
		scope = new ArrayScope(array);
	}

	public StructureJsonParser(JsonObject object) {
		scope = new ObjectScope(object);
	}

	@Override
	public boolean hasNext() {
		return scope.hasNext();
	}

	@Override
	public Event next() {
		current = scope.next();
		return current.getKey();
	}

	@Override
	public String getString() {
		if (current.getKey() == Event.KEY_NAME || current.getKey() == Event.VALUE_STRING) {
			return ((JsonString) current.getValue()).getString();
		}
		if (current.getKey() == Event.VALUE_NUMBER) {
			return ((JsonNumber) current.getValue()).toString();
		}
		throw new IllegalStateException(
				"JsonParser#getString() can only be called in KEY_NAME or VALUE_STRING states, not in "
						+ current.getKey());
	}

	@Override
	public boolean isIntegralNumber() {
		mustbeNumber();
		return ((JsonNumber) current.getValue()).isIntegral();
	}

	private void mustbeNumber() {
		if (current.getKey() != Event.VALUE_NUMBER) {
			throw new IllegalStateException("can only be called in VALUE_NUMBER state, not in " + current.getKey());
		}
	}

	@Override
	public int getInt() {
		mustbeNumber();
		return ((JsonNumber) current.getValue()).intValue();
	}

	@Override
	public long getLong() {
		mustbeNumber();
		return ((JsonNumber) current.getValue()).longValue();
	}

	@Override
	public BigDecimal getBigDecimal() {
		mustbeNumber();
		return ((JsonNumber) current.getValue()).bigDecimalValue();
	}

	@Override
	public JsonLocation getLocation() {
		return this;
	}

	@Override
	public void close() {
		// no need to close
	}

	@Override
	public JsonValue getValue() {
		return JsonParserImpl.getValue0(this, current.getKey());
	}

	@Override
	public void skipObject() {
		if (current.getKey() == Event.START_OBJECT) {
			int size = 0;
			while (next() != Event.END_OBJECT || size > 0) {
				if (current.getLeft() == Event.START_OBJECT) {
					size++;
				} else if (current.getLeft() == Event.END_OBJECT) {
					size--;
				}
			}
		}
	}

	@Override
	public void skipArray() {
		if (current.getKey() == Event.START_ARRAY) {
			int size = 0;
			while (next() != Event.END_ARRAY || size > 0) {
				if (current.getLeft() == Event.START_ARRAY) {
					size++;
				} else if (current.getLeft() == Event.END_ARRAY) {
					size--;
				}
			}
		}
	}

	private static class Scope implements Iterator<Pair<Event, JsonValue>> {

		private JsonValue value;
		private boolean has = true;

		public Scope(JsonValue value) {
			this.value = value;
		}

		@Override
		public boolean hasNext() {
			return has;
		}

		@Override
		public Pair<Event, JsonValue> next() {
			if (!has) {
				throw new NoSuchElementException();
			}
			has = false;
			return new Pair<>(CONVERT[value.getValueType().ordinal()], value);
		}

	}

	private abstract static class FillScope extends Scope {

		protected Pair<Event, JsonValue> event;
		private boolean end = false;
		private Queue<Scope> queue = new ArrayQueue<>();

		public FillScope(JsonValue value) {
			super(value);
		}

		protected abstract Pair<Event, JsonValue> getEnd();

		protected abstract void fill();

		@Override
		public boolean hasNext() {
			if (event == null && !end) {
				dequeue();
				if (event == null) {
					fill();
					dequeue();
				}
				if (event == null) {
					end = true;
					event = getEnd();
				}
			}
			return event != null;
		}

		private void dequeue() {
			while (event == null && !queue.isEmpty()) {
				Scope inner = queue.peek();
				if (inner.hasNext()) {
					event = inner.next();
				} else {
					queue.poll();
				}
			}
		}

		protected void enqueue(JsonValue value) {
			Scope newScope;
			if (value instanceof JsonArray) {
				newScope = new ArrayScope((JsonArray) value);
			} else if (value instanceof JsonObject) {
				newScope = new ObjectScope((JsonObject) value);
			} else {
				newScope = new Scope(value);
			}
			queue.offer(newScope);
		}

		@Override
		public Pair<Event, JsonValue> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			Pair<Event, JsonValue> pair = event;
			event = null;
			return pair;
		}

	}

	private static class ObjectScope extends FillScope {

		private Iterator<Entry<String, JsonValue>> it;

		public ObjectScope(JsonObject value) {
			super(value);
			event = new Pair<>(Event.START_OBJECT, null);
			it = value.entrySet().iterator();
		}

		@Override
		protected Pair<Event, JsonValue> getEnd() {
			return new Pair<>(Event.END_OBJECT, null);
		}

		@Override
		protected void fill() {
			if (it.hasNext()) {
				Entry<String, JsonValue> value = it.next();
				event = new Pair<>(Event.KEY_NAME, new JsonStringImpl(value.getKey()));
				enqueue(value.getValue());
			}
		}

	}

	private static class ArrayScope extends FillScope {

		private Iterator<JsonValue> it;

		public ArrayScope(JsonArray value) {
			super(value);
			event = new Pair<>(Event.START_ARRAY, null);
			it = value.iterator();
		}

		@Override
		protected Pair<Event, JsonValue> getEnd() {
			return new Pair<>(Event.END_ARRAY, null);
		}

		@Override
		protected void fill() {
			if (it.hasNext()) {
				enqueue(it.next());
			}
		}
	}

	@Override
	public long getLineNumber() {
		return 0;
	}

	@Override
	public long getColumnNumber() {
		return 0;
	}

	@Override
	public long getStreamOffset() {
		return 0;
	}

}
