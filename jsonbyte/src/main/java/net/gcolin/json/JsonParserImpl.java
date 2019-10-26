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
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonLocation;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;

import net.gcolin.common.collection.ArrayQueue;
import net.gcolin.common.io.Io;
import net.gcolin.common.lang.Strings;

/**
 * The {@code JsonParserImpl} class parses Json from a reader. The instances of JsonParserImpl are
 * pooled.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonParserImpl implements JsonParser, JsonLocation {

	interface State {
		boolean active(JsonParserImpl parser);

		default boolean next(JsonParserImpl parser) {
			throw new IllegalStateException("impossible");
		}
	}

	private static final String EXPECTING_U = " expecting 'u'";

	private static final String EXPECTING_R = " expecting 'r'";

	private static final String EXPECTING_E = " expecting 'e'";

	private static final String EXPECTING_S = " expecting 's'";

	private static final String EXPECTING_A = " expecting 'a'";

	private static final String EXPECTING_L = " expecting 'l'";

	// Table to look up hex ch -> value (for e.g HEX['F'] = 15, HEX['5'] = 5)
	private static final int[] HEX = new int[128];

	static {
		Arrays.fill(HEX, -1);
		for (int i = '0'; i <= '9'; i++) {
			HEX[i] = i - '0';
		}
		for (int i = 'A'; i <= 'F'; i++) {
			HEX[i] = 10 + i - 'A';
		}
		for (int i = 'a'; i <= 'f'; i++) {
			HEX[i] = 10 + i - 'a';
		}
	}

	private static final int HEX_LENGTH = HEX.length;

	private CharBuffer input = CharBuffer.allocate(1024);
	private Event event;
	private StringBuilder buffer = new StringBuilder();
	private Queue<Event> queue = new ArrayQueue<>();
	private boolean end = false;
	private boolean loaded = false;
	private long lineNumber = 1;
	private long columnNumber = 0;
	private long streamOffset = -1;
	private char current;
	private Reader source;
	private State state;
	private boolean readAll;

	private static final State[] BASIC_STATE = new State[126];
	private static final State[] COMA_STATE = new State[126];

	private static final State ROOT_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			throw new IllegalStateException("impossible");
		}

		@Override
		public boolean next(JsonParserImpl parser) {
			int read = parser.read0NoBlank(false);
			if (parser.end) {
				return false;
			}
			State nextState = null;
			if (read < 126) {
				nextState = BASIC_STATE[read];
			}
			if (nextState == null) {
				throw new JsonParsingException("bad json " + (char) read, parser);
			}
			if (nextState == NUMBER_SCOPE) {
				parser.current = (char) read;
			}
			return nextState.active(parser);
		}

	};

	private static final State COMA_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			throw new IllegalStateException("impossible");
		}

		@Override
		public boolean next(JsonParserImpl parser) {
			int read = parser.read0NoBlank(true);
			State nextState = null;
			if (read < 126) {
				nextState = COMA_STATE[read];
			}
			if (nextState == null) {
				if (read == ',') {
					if (parser.queue.peek() == Event.END_OBJECT) {
						return OBJECT_SCOPE.next(parser);
					} else {
						return ROOT_SCOPE.next(parser);
					}
				}
				throw new JsonParsingException("bad json " + (char) read, parser);
			}
			return nextState.active(parser);
		}

	};

	private static final State COMMENT_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			parser.skipComment();
			return !parser.end && ROOT_SCOPE.next(parser);
		}

	};

	private static final State COMA_COMMENT_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			parser.skipComment();
			return COMA_SCOPE.next(parser);
		}

	};

	private static final State OBJECT_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			if (parser.readAll) {
				throw new JsonParsingException("json should be shorter", parser);
			}
			parser.queue.offer(Event.END_OBJECT);
			parser.event = Event.START_OBJECT;
			parser.state = OBJECT_SCOPE;
			return true;
		}

		@Override
		public boolean next(JsonParserImpl parser) {
			parser.current = parser.read0NoBlank(true);

			State nextState = null;
			if (parser.current < 126) {
				nextState = BASIC_STATE[parser.current];
			}

			if (nextState == END_OBJECT_SCOPE) {
				return END_OBJECT_SCOPE.active(parser);
			} else if (nextState != STRING_SCOPE) {
				throw new JsonParsingException("exprected }", parser);
			} else {
				parser.readToken();
				parser.event = Event.KEY_NAME;
				parser.state = ROOT_SCOPE;
			}
			return true;
		}
	};

	private static final State END_OBJECT_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			if (parser.queue.poll() != Event.END_OBJECT) {
				throw new JsonParsingException("bad json", parser);
			}
			parser.event = Event.END_OBJECT;
			parser.endScope();
			return true;
		}

	};

	private static final State END_ARRAY_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			if (parser.queue.poll() != Event.END_ARRAY) {
				throw new JsonParsingException("bad json", parser);
			}
			parser.event = Event.END_ARRAY;
			parser.endScope();
			return true;
		}

	};

	private static final State ARRAY_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			if (parser.readAll) {
				throw new JsonParsingException("json should be shorter", parser);
			}
			parser.queue.offer(Event.END_ARRAY);
			parser.event = Event.START_ARRAY;
			parser.state = ROOT_SCOPE;
			return true;
		}
	};

	private static final State STRING_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			parser.readString();
			parser.endScope();
			return true;
		}
	};

	private static final State TRUE_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			parser.readTrue();
			parser.endScope();
			return true;
		}
	};

	private static final State FALSE_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			parser.readFalse();
			parser.endScope();
			return true;
		}
	};

	private static final State NULL_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			parser.readNull();
			parser.endScope();
			return true;
		}
	};

	private static final State NUMBER_SCOPE = new State() {

		public boolean active(JsonParserImpl parser) {
			parser.readNumber();
			parser.endScope();
			return true;
		}
	};

	private static final int POOL_SIZE = 50;
	private static final Queue<JsonParserImpl> POOL = new ArrayBlockingQueue<>(POOL_SIZE);

	static {
		COMA_STATE['/'] = COMA_COMMENT_SCOPE;
		COMA_STATE['}'] = END_OBJECT_SCOPE;
		COMA_STATE[']'] = END_ARRAY_SCOPE;

		BASIC_STATE['/'] = COMMENT_SCOPE;
		BASIC_STATE['"'] = STRING_SCOPE;
		BASIC_STATE['}'] = END_OBJECT_SCOPE;
		BASIC_STATE[']'] = END_ARRAY_SCOPE;
		for (int i = '0'; i <= '9'; i++) {
			BASIC_STATE[i] = NUMBER_SCOPE;
		}
		BASIC_STATE['-'] = NUMBER_SCOPE;
		BASIC_STATE['+'] = NUMBER_SCOPE;
		BASIC_STATE['t'] = TRUE_SCOPE;
		BASIC_STATE['n'] = NULL_SCOPE;
		BASIC_STATE['f'] = FALSE_SCOPE;
		BASIC_STATE['{'] = OBJECT_SCOPE;
		BASIC_STATE['['] = ARRAY_SCOPE;
	}

	/**
	 * Create a JsonParserImpl.
	 * 
	 * @param reader reader
	 */
	public JsonParserImpl(Reader reader) {
		source = reader;
		state = ROOT_SCOPE;
		input.position(input.capacity());
	}

	private void endScope() {
		if (queue.isEmpty()) {
			readAll = true;
			state = ROOT_SCOPE;
		} else {
			state = COMA_SCOPE;
		}
	}

	private void skipComment() {
		if (read0(true) != '*') {
			throw new JsonParsingException("bad json comment", this);
		}
		char prec = '\0';
		char ch;
		while (true) {
			ch = read0(true);
			if (ch == '/' && prec == '*') {
				break;
			} else {
				prec = ch;
			}
		}
	}

	public static JsonParserImpl take(InputStream in) {
		try {
			return take(Io.reader(in));
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Get or create a JsonParser from a pool.
	 * 
	 * @param reader a Reader
	 * @return a JSON parser
	 */
	public static JsonParserImpl take(Reader reader) {
		JsonParserImpl obj = POOL.poll();
		if (obj == null) {
			return new JsonParserImpl(reader);
		} else {
			obj.init(reader);
			return obj;
		}
	}

	/**
	 * initialize the parser.
	 * 
	 * @param reader a Reader
	 */
	public void init(Reader reader) {
		state = ROOT_SCOPE;
		source = reader;
		lineNumber = 1;
		columnNumber = 0;
		streamOffset = -1;
		buffer.setLength(0);
		end = false;
		loaded = false;
		queue.clear();
		event = null;
		input.clear();
		readAll = false;
		input.position(input.capacity());
	}

	private void readFalse() {
		char ch = read0(true);
		if (ch != 'a') {
			throw unexpectedChar(ch, EXPECTING_A);
		}
		ch = read0(true);
		if (ch != 'l') {
			throw unexpectedChar(ch, EXPECTING_L);
		}
		ch = read0(true);
		if (ch != 's') {
			throw unexpectedChar(ch, EXPECTING_S);
		}
		ch = read0(true);
		if (ch != 'e') {
			throw unexpectedChar(ch, EXPECTING_E);
		}
		event = Event.VALUE_FALSE;
	}

	private void readTrue() {
		char ch = read0(true);
		if (ch != 'r') {
			throw unexpectedChar(ch, EXPECTING_R);
		}
		ch = read0(true);
		if (ch != 'u') {
			throw unexpectedChar(ch, EXPECTING_U);
		}
		ch = read0(true);
		if (ch != 'e') {
			throw unexpectedChar(ch, EXPECTING_E);
		}
		event = Event.VALUE_TRUE;
	}

	private void readNull() {
		char ch = read0(true);
		if (ch != 'u') {
			throw unexpectedChar(ch, EXPECTING_U);
		}
		ch = read0(true);
		if (ch != 'l') {
			throw unexpectedChar(ch, EXPECTING_L);
		}
		ch = read0(true);
		if (ch != 'l') {
			throw unexpectedChar(ch, EXPECTING_L);
		}
		event = Event.VALUE_NULL;
	}

	private boolean isDigit(int ch) {
		return ch >= '0' && ch <= '9';
	}
	
	private char getsDigits() {
		char ch = '\0';
		while (!end) {
			ch = read0(false);
			if (!isDigit(ch)) {
				return ch;
			}
			buffer.append(ch);
		}
		return ch;
	}

	private void readNumber() {
		buffer.setLength(0);
		buffer.append(current);
		event = Event.VALUE_NUMBER;
		final long precColumn = columnNumber;
		final long precLineNumber = lineNumber;
		char ch = getsDigits();
		if (!end && ch == '.') {
			buffer.append(ch);
			ch = getsDigits();
		}
		if (!end && (ch == 'e' || ch == 'E')) {
			buffer.append('E');
			ch = read0(true);
			checkPlusOrMinus(ch);
			buffer.append(ch);
			ch = getsDigits();
		}
		if (!end && !isDigit(ch) && columnNumber != precColumn && streamOffset != precLineNumber) {
			rewind();
		}
	}

	private void rewind() {
		input.position(input.position() - 1);
		columnNumber--;
		streamOffset--;
	}

	private void checkPlusOrMinus(char ch) {
		if (ch != '+' && ch != '-' && !isDigit(ch)) {
			throw new JsonParsingException("bad number", this);
		}
	}

	private void readString() {
		buffer.setLength(0);
		event = Event.VALUE_STRING;

		char ch;
		while ((ch = read0(false)) != '"' && !end) {
			if (ch >= 0x20 && ch != 0x5c) {
				buffer.append(ch);
				continue;
			}
			if (ch == '\\') {
				unescape();
			} else {
				throw unexpectedChar(ch, null);
			}
		}
	}

	private void readToken() {
		buffer.setLength(0);
		buffer.append(current);
		event = Event.KEY_NAME;

		char ch = read0(false);
		if(buffer.charAt(0) != '"') {
			while (Strings.isBlank(ch)) {
				ch = read0(false);
			}
		}
		while (ch != ':' && !end) {
			if (ch >= 0x20 && ch != 0x5c) {
				buffer.append(ch);
				ch = read0(false);
				continue;
			}
			if (ch == '\\') {
				unescape();
				ch = read0(false);
			} else {
				throw unexpectedChar(ch, null);
			}
		}

		if (buffer.length() == 0) {
			throw new JsonParsingException("empty key name", this);
		} else if (buffer.charAt(0) == '"') {
			buffer.delete(0, 1);
			buffer.setLength(buffer.lastIndexOf("\""));
		}
	}

	private void unescape() {
		char ch = read0(true);
		if (ch == 'u') {
			unescapeUnicode();
		} else {
			char un = CharEncode.unescape(ch);
			if (un == '\0') {
				throw unexpectedChar(ch, null);
			} else {
				buffer.append(un);
			}
		}
	}

	private void unescapeUnicode() {
		int unicode = 0;
		for (int i = 0; i < 4; i++) {
			char ch3 = read0(true);
			int digit = ch3 < HEX_LENGTH ? HEX[ch3] : -1;
			if (digit < 0) {
				throw unexpectedChar(ch3, null);
			}
			unicode = (unicode << 4) | digit;
		}
		buffer.append((char) unicode);
	}

	private JsonParsingException unexpectedChar(char unexpected, String expected) {
		return new JsonParsingException("unexpected char=" + unexpected + Strings.toString(expected), this);
	}

	@Override
	public void close() {
		if (source != null) {
			Io.close(source);
			source = null;
			end = true;
			if (POOL.size() < POOL_SIZE) {
				POOL.offer(this);
			}
		}
	}

	@Override
	public BigDecimal getBigDecimal() {
		mustbe(JsonParser.Event.VALUE_NUMBER);
		return new BigDecimal(buffer.toString());
	}

	@Override
	public int getInt() {
		mustbe(JsonParser.Event.VALUE_NUMBER);
		return new BigDecimal(buffer.toString()).intValue();
	}

	@Override
	public JsonLocation getLocation() {
		return this;
	}

	@Override
	public long getLong() {
		mustbe(JsonParser.Event.VALUE_NUMBER);
		return Long.parseLong(buffer.toString());
	}

	@Override
	public String getString() {
		mustbe(JsonParser.Event.VALUE_STRING, JsonParser.Event.KEY_NAME, JsonParser.Event.VALUE_NUMBER);
		return buffer.toString();
	}

	@Override
	public boolean hasNext() {
		loadNext();
		return loaded;
	}

	@Override
	public boolean isIntegralNumber() {
		mustbe(JsonParser.Event.VALUE_NUMBER);
		BigDecimal bigDecimal = new BigDecimal(buffer.toString());
		return bigDecimal.scale() == 0;
	}

	private char read0(boolean throwE) {
		if (!input.hasRemaining()) {
			fillBuffer(throwE);
			if (end && !throwE) {
				return 'a';
			}
		}
		char ch = input.get();
		streamOffset++;
		if (ch == '\n') {
			columnNumber = 0;
			lineNumber++;
		} else {
			columnNumber++;
		}
		return ch;
	}

	private char read0NoBlank(boolean throwE) {
		char ch = ' ';
		while (Strings.isBlank(ch)) {
			if (!input.hasRemaining()) {
				fillBuffer(throwE);
				if (end && !throwE) {
					return 'a';
				}
			}
			ch = input.get();
			streamOffset++;
			if (ch == '\n') {
				columnNumber = 0;
				lineNumber++;
			} else {
				columnNumber++;
			}
		}
		return ch;
	}

	private void fillBuffer(boolean throwE) {
		input.clear();
		int nb;
		try {
			nb = source.read(input.array());
		} catch (IOException ex) {
			throw new JsonParsingException("cannot read source", ex, this);
		}
		if (nb <= 0) {
			end = true;
			if (throwE) {
				throw new JsonParsingException("json too short", this);
			}
		} else {
			input.position(nb);
			input.flip();
		}
	}

	@Override
	public Event next() {
		loadNext();
		if (!loaded) {
			throw new NoSuchElementException();
		}
		loaded = false;
		return event;
	}

	private void loadNext() {
		if (end || loaded) {
			return;
		}
		if (!input.hasRemaining()) {
			fillBuffer(false);
		}
		if (!end) {
			loaded = state.next(this);
		}
		if (!loaded && (!queue.isEmpty() || !readAll)) {
			throw new JsonParsingException("too short", this);
		}
	}

	private void mustbe(JsonParser.Event event) {
		if (this.event != event) {
			throw new IllegalStateException(
					"must be " + event + " parser state. But current parser state is " + this.event);
		}
	}

	private void mustbe(JsonParser.Event... event) {
		boolean find = false;
		for(Event evt : event) {
			if(this.event == evt) {
				find = true;
			}
		}
		if (!find) {
			throw new IllegalStateException(
					"must be " + Arrays.asList(event) + " parser state. But current parser state is " + this.event);
		}
	}

	@Override
	public long getLineNumber() {
		return lineNumber;
	}

	@Override
	public long getColumnNumber() {
		return columnNumber;
	}

	@Override
	public long getStreamOffset() {
		return streamOffset;
	}

	@Override
	public void skipObject() {
		if (event == Event.START_OBJECT) {
			int size = queue.size();
			while (next() != Event.END_OBJECT || queue.size() >= size);
		}
	}

	@Override
	public void skipArray() {
		if (event == Event.START_ARRAY) {
			int size = queue.size();
			while (next() != Event.END_ARRAY || queue.size() >= size);
		}
	}

	@Override
	public JsonValue getValue() {
		return getValue0(this, event);
	}

	public static JsonValue getValue0(JsonParser parser, Event event) {
		JsonValue out = null;
		switch (event) {
		case START_ARRAY:
			out = fillArray(new ReaderContext(parser));
			break;
		case START_OBJECT:
			out = fillObject(new ReaderContext(parser));
			break;
		case VALUE_FALSE:
			out = JsonValue.FALSE;
			break;
		case VALUE_TRUE:
			out = JsonValue.FALSE;
			break;
		case VALUE_NULL:
			out = JsonValue.NULL;
			break;
		case VALUE_NUMBER:
			out = new BigDecimalJsonNumber(parser.getBigDecimal());
			break;
		case VALUE_STRING:
			out = new JsonStringImpl(parser.getString());
			break;
		default:
			throw new JsonException("cannot get value of " + event);
		}
		return out;
	}

	public static JsonArray fillArray(ReaderContext ctx) {
		boolean cont = true;
		List<JsonValue> list = new ArrayList<>();
		List<JsonValue> old = ctx.values;
		ctx.values = list;
		while (cont && ctx.parser.hasNext()) {
			cont = ARRAY_ACTION[ctx.parser.next().ordinal()].apply(ctx);
		}
		ctx.values = old;
		return new JsonArrayImpl(list.toArray(new JsonValue[list.size()]));
	}

	public static JsonObject fillObject(ReaderContext ctx) {
		Map<String, JsonValue> map = new LinkedHashMap<>();
		Map<String, JsonValue> old = ctx.object;
		ctx.object = map;
		boolean cont = true;
		while (cont && ctx.parser.hasNext()) {
			cont = OBJECT_ACTION[ctx.parser.next().ordinal()].apply(ctx);
		}
		ctx.object = old;
		return new JsonObjectImpl(map);
	}

	private static final ParserReaderAction[] ARRAY_ACTION = new ParserReaderAction[Event.values().length];
	private static final ParserReaderAction[] OBJECT_ACTION = new ParserReaderAction[Event.values().length];

	private static void checkKey(ReaderContext ctx) {
		if (ctx.key == null) {
			throw new JsonParsingException("bad json object", ctx.parser.getLocation());
		}
	}

	static {
		ARRAY_ACTION[Event.VALUE_NUMBER.ordinal()] = r -> {
			r.values.add(new BigDecimalJsonNumber(r.parser.getBigDecimal()));
			return true;
		};
		ARRAY_ACTION[Event.VALUE_STRING.ordinal()] = r -> {
			r.values.add(new JsonStringImpl(r.parser.getString()));
			return true;
		};
		ARRAY_ACTION[Event.VALUE_FALSE.ordinal()] = r -> {
			r.values.add(JsonValue.FALSE);
			return true;
		};
		ARRAY_ACTION[Event.VALUE_NULL.ordinal()] = r -> {
			r.values.add(JsonValue.NULL);
			return true;
		};
		ARRAY_ACTION[Event.VALUE_TRUE.ordinal()] = r -> {
			r.values.add(JsonValue.TRUE);
			return true;
		};
		ARRAY_ACTION[Event.END_ARRAY.ordinal()] = r -> false;
		ARRAY_ACTION[Event.START_ARRAY.ordinal()] = r -> {
			r.values.add(fillArray(r));
			return true;
		};
		ARRAY_ACTION[Event.START_OBJECT.ordinal()] = r -> {
			r.values.add(fillObject(r));
			return true;
		};

		OBJECT_ACTION[Event.KEY_NAME.ordinal()] = r -> {
			r.key = r.parser.getString();
			return true;
		};

		OBJECT_ACTION[Event.VALUE_NUMBER.ordinal()] = r -> {
			checkKey(r);
			r.object.put(r.key, new BigDecimalJsonNumber(r.parser.getBigDecimal()));
			r.key = null;
			return true;
		};

		OBJECT_ACTION[Event.VALUE_STRING.ordinal()] = r -> {
			checkKey(r);
			r.object.put(r.key, new JsonStringImpl(r.parser.getString()));
			r.key = null;
			return true;
		};

		OBJECT_ACTION[Event.VALUE_FALSE.ordinal()] = r -> {
			checkKey(r);
			r.object.put(r.key, JsonValue.FALSE);
			r.key = null;
			return true;
		};

		OBJECT_ACTION[Event.VALUE_TRUE.ordinal()] = r -> {
			checkKey(r);
			r.object.put(r.key, JsonValue.TRUE);
			r.key = null;
			return true;
		};

		OBJECT_ACTION[Event.VALUE_NULL.ordinal()] = r -> {
			checkKey(r);
			r.object.put(r.key, JsonValue.NULL);
			r.key = null;
			return true;
		};

		OBJECT_ACTION[Event.END_OBJECT.ordinal()] = r -> false;

		OBJECT_ACTION[Event.START_ARRAY.ordinal()] = r -> {
			checkKey(r);
			String key = r.key;
			r.key = null;
			r.object.put(key, fillArray(r));
			return true;
		};

		OBJECT_ACTION[Event.START_OBJECT.ordinal()] = r -> {
			checkKey(r);
			String key = r.key;
			r.key = null;
			r.object.put(key, fillObject(r));
			return true;
		};
	}

}
