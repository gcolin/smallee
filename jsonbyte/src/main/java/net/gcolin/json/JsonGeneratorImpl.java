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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.BiConsumer;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonGenerationException;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser.Event;

import net.gcolin.common.collection.ArrayQueue;
import net.gcolin.common.io.Io;

/**
 * The {@code JsonGeneratorImpl} class is a JsonGenerator.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonGeneratorImpl implements JsonGenerator {

	private static final char BR_O = '[';
	private static final char BR_C = ']';
	private static final char AC_O = '{';
	private static final char AC_C = '}';
	private static final char QUOTE = '"';
	private static final char[] NULL = "null".toCharArray();
	private static final int NULL_LEN = NULL.length;
	private static final char[] FALSE = "false".toCharArray();
	private static final int FALSE_LEN = FALSE.length;
	private static final char[] TRUE = "true".toCharArray();
	private static final int TRUE_LEN = TRUE.length;
	public static final String CANNOT_WRITE_ON_MORE_JSON_TEXT = "cannot write on more JSON Text";
	private Queue<Event> events = new ArrayQueue<>();
	protected CharBuffer buffer = CharBuffer.allocate(1024);
	protected boolean generated;
	private boolean key;
	protected Writer source;
	@SuppressWarnings("unchecked")
	private static final BiConsumer<JsonValue, JsonGeneratorImpl>[] ARRAYS = new BiConsumer[ValueType.values().length];

	private static final int POOL_SIZE = 50;
	private static final Queue<JsonGeneratorImpl> POOL = new ArrayBlockingQueue<>(POOL_SIZE);

	static {
		ARRAYS[ValueType.ARRAY.ordinal()] = (val, ctx) -> {
			JsonArray array = (JsonArray) val;
			ctx.startArray();
			for (int i = 0, l = array.size(); i < l; i++) {
				ctx.write(array.get(i));
			}
			ctx.writeEnd();
		};
		ARRAYS[ValueType.OBJECT.ordinal()] = (val, ctx) -> {
			JsonObject object = (JsonObject) val;
			ctx.startObject();
			for (Entry<String, JsonValue> e : object.entrySet()) {
				ctx.write(e.getKey(), e.getValue());
			}
			ctx.writeEnd();
		};
		ARRAYS[ValueType.FALSE.ordinal()] = (val, ctx) -> ctx.put(FALSE, 0, FALSE_LEN);
		ARRAYS[ValueType.TRUE.ordinal()] = (val, ctx) -> ctx.put(TRUE, 0, TRUE_LEN);
		ARRAYS[ValueType.NULL.ordinal()] = (val, ctx) -> ctx.put(NULL, 0, NULL_LEN);
		ARRAYS[ValueType.NUMBER.ordinal()] = (val, ctx) -> ctx.put(val.toString());
		ARRAYS[ValueType.STRING.ordinal()] = (val, ctx) -> ctx.writeString(((JsonString) val).getString());
	}

	public JsonGeneratorImpl(Writer source) {
		this.source = source;
	}

	private void put(String str) {
		put(str.toCharArray());
	}

	private void put(char[] ca) {
		put(ca, 0, ca.length);
	}

	private void put(String str, int off, int len) {
		put(str.toCharArray(), off, len);
	}

	private void put(char[] ca, int off, int len) {
		if (buffer.remaining() >= len) {
			buffer.put(ca, off, len);
		} else {
			int rem = len;
			int offset = off;
			while (rem > 0) {
				int nb = Math.min(buffer.remaining(), rem);
				buffer.put(ca, offset, nb);
				rem -= nb;
				offset += nb;
				if (!buffer.hasRemaining()) {
					flushBuffer();
				}
			}
		}
	}

	private void put(char ch) {
		if (!buffer.hasRemaining()) {
			flushBuffer();
		}
		buffer.put(ch);
	}

	private void flushBuffer() {
		buffer.flip();
		try {
			source.write(buffer.array(), 0, buffer.limit());
		} catch (IOException ex) {
			throw new JsonGenerationException(ex.getMessage(), ex);
		}
		buffer.clear();
	}

	@Override
	public void close() {
		if (buffer.position() > 0) {
			flushBuffer();
		}
		Io.close(source);
		if (!generated) {
			throw new JsonGenerationException("no JSON is generated");
		}
		if (!events.isEmpty()) {
			throw new JsonGenerationException("writeEnd() not called");
		}
		if (source == null) {
			return;
		}
		this.source = null;
		this.events.clear();
		recycle();
	}

	protected void recycle() {
		source = null;
		POOL.offer(this);
	}

	public static JsonGeneratorImpl take(OutputStream source) {
		return take(Io.writer(source, StandardCharsets.UTF_8.name()));
	}

	/**
	 * Get a JsonGenerator from the pool or create a new JsonGenerator.
	 * 
	 * @param source a writer
	 * @return a JSON generator
	 */
	public static JsonGeneratorImpl take(Writer source) {
		JsonGeneratorImpl jg = POOL.poll();
		if (jg == null) {
			return new JsonGeneratorImpl(source);
		} else {
			jg.source = source;
			jg.generated = false;
			return jg;
		}
	}

	@Override
	public void flush() {
		if (buffer.position() > 0) {
			flushBuffer();
		}
		try {
			source.flush();
		} catch (IOException ex) {
			throw new JsonGenerationException(ex.getMessage(), ex);
		}
	}

	private void objectCheck(String key) {
		if (!generated || events.isEmpty()) {
			throw new JsonGenerationException("missing start object or you try to write 2 jsons");
		}
		if (this.key) {
			throw new JsonGenerationException("Field value, start object/array expected");
		} else if (events.peek() != Event.START_OBJECT && events.peek() != Event.END_OBJECT) {
			throw new JsonGenerationException("you are not in an object");
		}
		writeComma();
		writeString(key);
		put(':');
	}

	protected void writeComma() {
		if (events.peek() == Event.END_OBJECT) {
			put(',');
		} else {
			events.offer(Event.END_OBJECT);
		}
	}

	private void arrayCheck() {
		if (!generated) {
			generated = true;
			return;
		}
		if (events.isEmpty()) {
			throw new JsonGenerationException("missing start array or you try to write 2 jsons");
		}
		if (key && (events.peek() == Event.START_OBJECT || events.peek() == Event.END_OBJECT)) {
			key = false;
		} else if (events.peek() != Event.START_ARRAY && events.peek() != Event.END_OBJECT) {
			throw new JsonGenerationException("you are not in an array");
		} else {
			writeComma();
		}
	}

	/**
	 * Write a Boolean.
	 * 
	 * @param val Boolean
	 */
	public void write0(boolean val) {
		writeComma();
		if (val) {
			put(TRUE, 0, TRUE_LEN);
		} else {
			put(FALSE, 0, FALSE_LEN);
		}
	}

	/**
	 * Write a Boolean.
	 * 
	 * @param key key
	 * @param val Boolean
	 */
	public void write0(char[] key, boolean val) {
		writeComma();
		put(key, 0, key.length);
		if (val) {
			put(TRUE, 0, TRUE_LEN);
		} else {
			put(FALSE, 0, FALSE_LEN);
		}
	}

	/**
	 * Write an Integer.
	 * 
	 * @param str Integer
	 */
	public void write0(int str) {
		writeComma();
		writeInt(str);
	}

	/**
	 * Write an Integer.
	 * 
	 * @param key key
	 * @param str Integer
	 */
	public void write0(char[] key, int str) {
		writeComma();
		put(key, 0, key.length);
		writeInt(str);
	}

	/**
	 * Write a Long.
	 * 
	 * @param str long
	 */
	public void write0(long str) {
		writeComma();
		writeLong(str);
	}

	/**
	 * Write a Long.
	 * 
	 * @param key key
	 * @param str long
	 */
	public void write0(char[] key, long str) {
		writeComma();
		put(key, 0, key.length);
		writeLong(str);
	}

	/**
	 * Write a String.
	 * 
	 * @param str String
	 */
	public void write0(String str) {
		writeComma();
		put(str);
	}

	/**
	 * Write a String.
	 * 
	 * @param key key
	 * @param str String
	 */
	public void write0(char[] key, String str) {
		writeComma();
		put(key, 0, key.length);
		put(str);
	}

	/**
	 * Write a JsonValue.
	 * 
	 * @param key the key
	 * @param val the JsonValue
	 */
	public void write0(char[] key, JsonValue val) {
		writeComma();
		put(key, 0, key.length);
		ARRAYS[val.getValueType().ordinal()].accept(val, this);
	}

	/**
	 * Write a String.
	 * 
	 * @param str String
	 */
	public void write0Quoted(String str) {
		writeComma();
		put(QUOTE);
		put(str);
		put(QUOTE);
	}

	/**
	 * Write a String.
	 * 
	 * @param key key
	 * @param str string
	 */
	public void write0Quoted(char[] key, String str) {
		writeComma();
		put(key, 0, key.length);
		put(QUOTE);
		put(str);
		put(QUOTE);
	}

	/**
	 * Write start array without check.
	 */
	public void writeStartArray0() {
		writeComma();
		startArray();
	}

	/**
	 * Write start array without check.
	 * 
	 * @param key the key
	 */
	public void writeStartArray0(char[] key) {
		writeComma();
		put(key);
		startArray();
	}

	/**
	 * Write start object without check.
	 */
	public void writeStartObject0() {
		writeComma();
		startObject();
	}

	/**
	 * Write start object without check.
	 * 
	 * @param key the key
	 */
	public void writeStartObject0(char[] key) {
		writeComma();
		put(key, 0, key.length);
		startObject();
	}

	@Override
	public JsonGenerator write(JsonValue val) {
		arrayCheck();
		ARRAYS[val.getValueType().ordinal()].accept(val, this);
		return this;
	}

	@Override
	public JsonGenerator write(String val) {
		arrayCheck();
		writeString(val);
		return this;
	}

	@Override
	public JsonGenerator write(BigDecimal val) {
		arrayCheck();
		put(val.toString());
		return this;
	}

	@Override
	public JsonGenerator write(BigInteger val) {
		arrayCheck();
		put(val.toString());
		return this;
	}

	@Override
	public JsonGenerator write(int val) {
		arrayCheck();
		writeInt(val);
		return this;
	}

	@Override
	public JsonGenerator write(long val) {
		arrayCheck();
		writeLong(val);
		return this;
	}

	@Override
	public JsonGenerator write(double val) {
		arrayCheck();
		doubleCheck(val);
		put(String.valueOf(val));
		return this;
	}

	@Override
	public JsonGenerator write(boolean val) {
		arrayCheck();
		if (val) {
			put(TRUE, 0, TRUE_LEN);
		} else {
			put(FALSE, 0, FALSE_LEN);
		}
		return this;
	}

	@Override
	public JsonGenerator write(String key, JsonValue val) {
		objectCheck(key);
		ARRAYS[val.getValueType().ordinal()].accept(val, this);
		return this;
	}

	@Override
	public JsonGenerator write(String key, String val) {
		objectCheck(key);
		writeString(val);
		return this;
	}

	@Override
	public JsonGenerator write(String key, BigInteger val) {
		objectCheck(key);
		put(val.toString());
		return this;
	}

	@Override
	public JsonGenerator write(String key, BigDecimal val) {
		objectCheck(key);
		put(val.toString());
		return this;
	}

	@Override
	public JsonGenerator write(String key, int val) {
		objectCheck(key);
		writeInt(val);
		return this;
	}

	@Override
	public JsonGenerator write(String key, long val) {
		objectCheck(key);
		writeLong(val);
		return this;
	}

	@Override
	public JsonGenerator write(String key, double val) {
		objectCheck(key);
		doubleCheck(val);
		put(String.valueOf(val));
		return this;
	}

	@Override
	public JsonGenerator write(String key, boolean val) {
		objectCheck(key);
		if (val) {
			put(TRUE, 0, TRUE_LEN);
		} else {
			put(FALSE, 0, FALSE_LEN);
		}
		return this;
	}

	@Override
	public JsonGenerator writeEnd() {
		if (events.isEmpty()) {
			throw new JsonGenerationException("cannot end something which never begins");
		}
		writeEnd0();
		return this;
	}

	/**
	 * Write end without check.
	 */
	public void writeEnd0() {
		Event event = events.poll();
		if (event == Event.END_OBJECT) {
			event = events.poll();
		}
		if (event == Event.START_ARRAY) {
			put(BR_C);
		} else {
			put(AC_C);
		}
		if (events.isEmpty()) {
			flush();
		}
	}

	@Override
	public JsonGenerator writeNull() {
		arrayCheck();
		put(NULL, 0, NULL_LEN);
		return this;
	}

	@Override
	public JsonGenerator writeNull(String key) {
		objectCheck(key);
		put(NULL, 0, NULL_LEN);
		return this;
	}

	@Override
	public JsonGenerator writeStartArray() {
		arrayCheck();
		startArray();
		return this;
	}

	@Override
	public JsonGenerator writeStartArray(String key) {
		objectCheck(key);
		startArray();
		return this;
	}

	protected void startArray() {
		generated = true;
		put(BR_O);
		events.offer(Event.START_ARRAY);
	}

	@Override
	public JsonGenerator writeStartObject() {
		arrayCheck();
		startObject();
		return this;
	}

	@Override
	public JsonGenerator writeStartObject(String key) {
		objectCheck(key);
		startObject();
		return this;
	}

	protected void startObject() {
		generated = true;
		put(AC_O);
		events.offer(Event.START_OBJECT);
	}

	private void writeString(String val) {
		put(QUOTE);
		for (int i = 0, len = val.length(); i < len; ++i) {
			int begin = i;
			int end = i;
			char ch = val.charAt(i);
			// find all the characters that need not be escaped
			// unescaped = %x20-21 | %x23-5B | %x5D-10FFFF
			while (ch >= 0x20 && ch != 0x22 && ch != 0x5c) {
				end = ++i;
				if (i < len) {
					ch = val.charAt(i);
				} else {
					break;
				}
			}
			// Write characters without escaping
			if (begin < end) {
				put(val, begin, end - begin);
				if (i == len) {
					break;
				}
			}

			put(CharEncode.escape(ch));
		}
		put(QUOTE);
	}

	private void doubleCheck(double value) {
		if (Double.isInfinite(value) || Double.isNaN(value)) {
			throw new NumberFormatException("write(String, double) value cannot be Infinite or NaN");
		}
	}

	private void writeInt(int nb) {
		put(Integer.toString(nb));
	}

	private void writeLong(long nb) {
		put(Long.toString(nb));
	}

	@Override
	public JsonGenerator writeKey(String name) {
		objectCheck(name);
		key = true;
		return this;
	}

}
