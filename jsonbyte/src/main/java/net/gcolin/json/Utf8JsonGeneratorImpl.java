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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

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
 * The {@code Utf8JsonGeneratorImpl} class is a JsonGenerator.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Utf8JsonGeneratorImpl implements JsonGenerator {

  interface JAction {
    void accept(JsonValue value, Utf8JsonGeneratorImpl ctx) throws IOException;
  }

  private static final int[] sOutputEscapes;
  private static final byte[] HEX_CHARS;

  static {
    char[] hc = "0123456789ABCDEF".toCharArray();
    int len = hc.length;
    HEX_CHARS = new byte[len];
    for (int i = 0; i < len; ++i) {
      HEX_CHARS[i] = (byte) hc[i];
    }

    int[] table = new int[128];
    // Control chars need generic escape sequence
    for (int i = 0; i < 32; ++i) {
      // 04-Mar-2011, tatu: Used to use "-(i + 1)", replaced with constant
      table[i] = -1;
    }
    /*
     * Others (and some within that range too) have explicit shorter sequences
     */
    table['"'] = '"';
    table['\\'] = '\\';
    // Escaping of slash is optional, so let's not add it
    table[0x08] = 'b';
    table[0x09] = 't';
    table[0x0C] = 'f';
    table[0x0A] = 'n';
    table[0x0D] = 'r';
    sOutputEscapes = table;
  }

  public static final int SURR1_FIRST = 0xD800;
  public static final int SURR1_LAST = 0xDBFF;
  public static final int SURR2_FIRST = 0xDC00;
  public static final int SURR2_LAST = 0xDFFF;

  private static final byte BR_O = '[';
  private static final byte BR_C = ']';
  private static final byte AC_O = '{';
  private static final byte AC_C = '}';
  private static final byte QUOTE = '"';
  private static final byte[] NULL = "null".getBytes(StandardCharsets.UTF_8);
  private static final int NULL_LEN = NULL.length;
  private static final byte[] FALSE = "false".getBytes(StandardCharsets.UTF_8);
  private static final int FALSE_LEN = FALSE.length;
  private static final byte[] TRUE = "true".getBytes(StandardCharsets.UTF_8);
  private static final int TRUE_LEN = TRUE.length;
  private static final byte[] BYTE_BACKSLASH_U = "\\u".getBytes(StandardCharsets.UTF_8);
  private static final int BYTE_BACKSLASH_U_LEN = BYTE_BACKSLASH_U.length;
  private static final byte[] BYTE_00 = "00".getBytes(StandardCharsets.UTF_8);
  private static final int BYTE_00_LEN = BYTE_00.length;
  public static final String CANNOT_WRITE_ON_MORE_JSON_TEXT = "cannot write on more JSON Text";
  private Queue<Event> events = new ArrayQueue<>();
  protected boolean generated;
  protected OutputStream source;
  private boolean key;
  private static final JAction[] ARRAYS = new JAction[ValueType.values().length];

  private static final int POOL_SIZE = 50;
  private static final Queue<Utf8JsonGeneratorImpl> POOL = new ArrayBlockingQueue<>(POOL_SIZE);

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
    ARRAYS[ValueType.FALSE.ordinal()] = (val, ctx) -> ctx.source.write(FALSE, 0, FALSE_LEN);
    ARRAYS[ValueType.TRUE.ordinal()] = (val, ctx) -> ctx.source.write(TRUE, 0, TRUE_LEN);
    ARRAYS[ValueType.NULL.ordinal()] = (val, ctx) -> ctx.source.write(NULL, 0, NULL_LEN);
    ARRAYS[ValueType.NUMBER.ordinal()] = (val, ctx) -> ctx.put(val.toString());
    ARRAYS[ValueType.STRING.ordinal()] =
        (val, ctx) -> ctx.writeString(((JsonString) val).getString());
  }

  public Utf8JsonGeneratorImpl(OutputStream source) {
    this.source = source;
  }

  private void put(String str) throws IOException {
    put(str.toCharArray());
  }

  private void put(char[] ca) throws IOException {
    put(ca, 0, ca.length);
  }

  private void put(String str, int off, int len) throws IOException {
    put(str.toCharArray(), off, len);
  }

  private void put(char[] ca, int off, int len) throws IOException {
    final int[] escCodes = sOutputEscapes;
    OutputStream out = source;
    while (off < len) {
      int ch = ca[off];
      // note: here we know that (ch > 0x7F) will cover case of escaping non-ASCII too:
      if (ch > 0x7F || escCodes[ch] != 0) {
        break;
      }
      out.write(ch);
      off++;
    }
    if (off < len) {
      put2(ca, off, len);
    }
  }

  /**
   * Secondary method called when content contains characters to escape, and/or multi-byte UTF-8
   * characters.
   */
  private void put2(final char[] cbuf, int offset, final int end) throws IOException {
    final int[] escCodes = sOutputEscapes;
    OutputStream out = source;

    while (offset < end) {
      int ch = cbuf[offset++];
      if (ch <= 0x7F) {
        if (escCodes[ch] == 0) {
          out.write(ch);
          continue;
        }
        int escape = escCodes[ch];
        if (escape > 0) { // 2-char escape, fine
          out.write('\\');
          out.write(escape);
        } else {
          // ctrl-char, 6-byte escape...
          writeGenericEscape(ch);
        }
        continue;
      }
      if (ch <= 0x7FF) { // fine, just needs 2 byte output
        source.write(0xc0 | (ch >> 6));
        source.write(0x80 | (ch & 0x3f));
      } else {
        outputMultiByteChar(ch);
      }
    }
  }

  private void outputMultiByteChar(int ch) throws IOException {
    if (ch >= SURR1_FIRST && ch <= SURR2_LAST) { // yes, outside of BMP; add an escape
      // 23-Nov-2015, tatu: As per [core#223], may or may not want escapes;
      // it would be added here... but as things are, we do not have proper
      // access yet...
      source.write(BYTE_BACKSLASH_U, 0, BYTE_BACKSLASH_U_LEN);

      source.write(HEX_CHARS[(ch >> 12) & 0xF]);
      source.write(HEX_CHARS[(ch >> 8) & 0xF]);
      source.write(HEX_CHARS[(ch >> 4) & 0xF]);
      source.write(HEX_CHARS[ch & 0xF]);
    } else {
      source.write(0xe0 | (ch >> 12));
      source.write(0x80 | ((ch >> 6) & 0x3f));
      source.write(0x80 | (ch & 0x3f));
    }
  }

  private void writeGenericEscape(int charToEscape) throws IOException {
    source.write(BYTE_BACKSLASH_U, 0, BYTE_BACKSLASH_U_LEN);
    if (charToEscape > 0xFF) {
      int hi = (charToEscape >> 8) & 0xFF;
      source.write(HEX_CHARS[hi >> 4]);
      source.write(HEX_CHARS[hi & 0xF]);
      charToEscape &= 0xFF;
    } else {
      source.write(BYTE_00, 0, BYTE_00_LEN);
    }
    // We know it's a control char, so only the last 2 chars are non-0
    source.write(HEX_CHARS[charToEscape >> 4]);
    source.write(HEX_CHARS[charToEscape & 0xF]);
  }

  @Override
  public void close() {
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

  /**
   * Get a JsonGenerator from the pool or create a new JsonGenerator.
   * 
   * @param source a writer
   * @return a JSON generator
   */
  public static Utf8JsonGeneratorImpl take(OutputStream source) {
    Utf8JsonGeneratorImpl jg = POOL.poll();
    if (jg == null) {
      return new Utf8JsonGeneratorImpl(source);
    } else {
      jg.source = source;
      jg.generated = false;
      return jg;
    }
  }

  @Override
  public void flush() {
    try {
      source.flush();
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
  }

  private void objectCheck(String key) throws IOException {
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
    source.write(':');
  }

  protected void writeComma() throws IOException {
    if (events.peek() == Event.END_OBJECT) {
      source.write(',');
    } else {
      events.offer(Event.END_OBJECT);
    }
  }

  private void arrayCheck() throws IOException {
    if (!generated) {
      return;
    }
    if (events.isEmpty()) {
      throw new JsonGenerationException("missing start array or you try to write 2 jsons");
    }
    
    if(key && (events.peek() == Event.START_OBJECT || events.peek() == Event.END_OBJECT)) {
      key = false;
    } else if (events.peek() != Event.START_ARRAY && events.peek() != Event.END_OBJECT) {
      throw new JsonGenerationException("you are not in an array");
    } else {
      writeComma();
    }
  }

  @Override
  public JsonGenerator write(JsonValue val) {
    try {
      arrayCheck();
      ARRAYS[val.getValueType().ordinal()].accept(val, this);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(String val) {
    try {
      arrayCheck();
      writeString(val);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(BigDecimal val) {
    try {
      arrayCheck();
      put(val.toString());
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(BigInteger val) {
    try {
      arrayCheck();
      put(val.toString());
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(int val) {
    try {
      arrayCheck();
      writeInt(val);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(long val) {
    try {
      arrayCheck();
      writeLong(val);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(double val) {
    try {
      arrayCheck();
      doubleCheck(val);
      put(String.valueOf(val));
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(boolean val) {
    try {
      arrayCheck();
      if (val) {
        source.write(TRUE, 0, TRUE_LEN);
      } else {
        source.write(FALSE, 0, FALSE_LEN);
      }
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(String key, JsonValue val) {
    try {
      objectCheck(key);
      ARRAYS[val.getValueType().ordinal()].accept(val, this);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(String key, String val) {
    try {
      objectCheck(key);
      writeString(val);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(String key, BigInteger val) {
    try {
      objectCheck(key);
      put(val.toString());
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  /**
   * Write bytes.
   * 
   * @param data data
   * @throws IOException is an error occurs.
   */
  public void write(byte[] data) throws IOException {
    source.write(data);
  }

  @Override
  public JsonGenerator write(String key, BigDecimal val) {
    try {
      objectCheck(key);
      put(val.toString());
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(String key, int val) {
    try {
      objectCheck(key);
      writeInt(val);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(String key, long val) {
    try {
      objectCheck(key);
      writeLong(val);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(String key, double val) {
    try {
      objectCheck(key);
      doubleCheck(val);
      put(String.valueOf(val));
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator write(String key, boolean val) {
    try {
      objectCheck(key);
      if (val) {
        source.write(TRUE, 0, TRUE_LEN);
      } else {
        source.write(FALSE, 0, FALSE_LEN);
      }
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  /**
   * Write a Boolean.
   * 
   * @param val Boolean
   * @throws IOException is an error occurs.
   */
  public void write0(boolean val) throws IOException {
    writeComma();
    if (val) {
      source.write(TRUE, 0, TRUE_LEN);
    } else {
      source.write(FALSE, 0, FALSE_LEN);
    }
  }

  /**
   * Write a Boolean.
   * 
   * @param val Boolean
   * @param key key
   * @throws IOException is an error occurs.
   */
  public void write0(byte[] key, boolean val) throws IOException {
    writeComma();
    source.write(key);
    if (val) {
      source.write(TRUE, 0, TRUE_LEN);
    } else {
      source.write(FALSE, 0, FALSE_LEN);
    }
  }

  /**
   * Write a Integer.
   * 
   * @param str Integer
   * @throws IOException is an error occurs.
   */
  public void write0(int str) throws IOException {
    writeComma();
    writeInt(str);
  }

  /**
   * Write a Integer.
   * 
   * @param str Integer
   * @param key key
   * @throws IOException is an error occurs.
   */
  public void write0(byte[] key, int str) throws IOException {
    writeComma();
    source.write(key);
    writeInt(str);
  }

  /**
   * Write a Long.
   * 
   * @param str Long
   * @throws IOException is an error occurs.
   */
  public void write0(long str) throws IOException {
    writeComma();
    writeLong(str);
  }

  /**
   * Write a Long.
   * 
   * @param str Long
   * @param key key
   * @throws IOException is an error occurs.
   */
  public void write0(byte[] key, long str) throws IOException {
    writeComma();
    source.write(key);
    writeLong(str);
  }

  /**
   * Write a String.
   * 
   * @param str String
   * @throws IOException is an error occurs.
   */
  public void write0(String str) throws IOException {
    writeComma();
    put(str);
  }

  /**
   * Write a String.
   * 
   * @param str String
   * @param key key
   * @throws IOException is an error occurs.
   */
  public void write0(byte[] key, String str) throws IOException {
    writeComma();
    source.write(key);
    put(str);
  }

  /**
   * Write a JsonValue.
   * 
   * @param val JsonValue
   * @param key key
   * @throws IOException is an error occurs.
   */
  public void write0(byte[] key, JsonValue val) throws IOException {
    writeComma();
    source.write(key);
    ARRAYS[val.getValueType().ordinal()].accept(val, this);
  }

  /**
   * Write a String with quotes.
   * 
   * @param str String
   * @throws IOException is an error occurs.
   */
  public void write0Quoted(String str) throws IOException {
    writeComma();
    source.write(QUOTE);
    put(str);
    source.write(QUOTE);
  }

  /**
   * Write a String with quotes.
   * 
   * @param str String
   * @param key key
   * @throws IOException is an error occurs.
   */
  public void write0Quoted(byte[] key, String str) throws IOException {
    writeComma();
    source.write(key);
    source.write(QUOTE);
    put(str);
    source.write(QUOTE);
  }

  /**
   * Write a start array.
   * 
   * @throws IOException is an error occurs.
   */
  public void writeStartArray0() throws IOException {
    writeComma();
    startArray();
  }

  /**
   * Write a start array.
   * 
   * @param key key
   * @throws IOException is an error occurs.
   */
  public void writeStartArray0(byte[] key) throws IOException {
    writeComma();
    source.write(key);
    startArray();
  }

  /**
   * Write a start object.
   * 
   * @throws IOException is an error occurs.
   */
  public void writeStartObject0() throws IOException {
    writeComma();
    startObject();
  }

  /**
   * Write a start object.
   * 
   * @param key key
   * @throws IOException is an error occurs.
   */
  public void writeStartObject0(byte[] key) throws IOException {
    writeComma();
    source.write(key);
    startObject();
  }

  /**
   * Write end.
   * 
   * @throws IOException is an error occurs.
   */
  public void writeEnd0() throws IOException {
    Event event = events.poll();
    if (event == Event.END_OBJECT) {
      event = events.poll();
    }
    if (event == Event.START_ARRAY) {
      source.write(BR_C);
    } else {
      source.write(AC_C);
    }
    if (events.isEmpty()) {
      flush();
    }
  }

  @Override
  public JsonGenerator writeEnd() {
    if (events.isEmpty()) {
      throw new JsonGenerationException("cannot end something which never begins");
    }
    try {
      writeEnd0();
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator writeNull() {
    try {
      arrayCheck();
      source.write(NULL, 0, NULL_LEN);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator writeNull(String key) {
    try {
      objectCheck(key);
      source.write(NULL, 0, NULL_LEN);
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator writeStartArray() {
    try {
      arrayCheck();
      startArray();
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator writeStartArray(String key) {
    try {
      objectCheck(key);
      startArray();
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  protected void startArray() throws IOException {
    generated = true;
    source.write(BR_O);
    events.offer(Event.START_ARRAY);
  }

  @Override
  public JsonGenerator writeStartObject() {
    try {
      arrayCheck();
      startObject();
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  @Override
  public JsonGenerator writeStartObject(String key) {
    try {
      objectCheck(key);
      startObject();
    } catch (IOException ex) {
      throw new JsonGenerationException(ex.getMessage(), ex);
    }
    return this;
  }

  protected void startObject() throws IOException {
    generated = true;
    source.write(AC_O);
    events.offer(Event.START_OBJECT);
  }

  private void writeString(String val) throws IOException {
    source.write('"');
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
    source.write('"');
  }

  private void doubleCheck(double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      throw new NumberFormatException("write(String, double) value cannot be Infinite or NaN");
    }
  }

  private static final byte MINUS = '-';
  private static final byte[] DigitTens = {'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1',
      '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
      '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4',
      '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6',
      '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8',
      '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',};

  private static final byte[] DigitOnes = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
      '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8',
      '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',};

  private static final byte[] digits =
      {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
          'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

  private byte[] numberbuf = new byte[20];
  private static final byte[] minValue = "-2147483648".getBytes(StandardCharsets.UTF_8);
  private static final byte[] longminValue =
      "-9223372036854775808".getBytes(StandardCharsets.UTF_8);

  private void writeInt(int nb) throws IOException {
    if (nb == Integer.MIN_VALUE) {
      source.write(minValue, 0, minValue.length);
      return;
    }
    int qo;
    int re;
    int charPos = 19;
    byte[] buf = numberbuf;
    boolean minus = false;

    if (nb < 0) {
      minus = true;
      nb = -nb;
    }

    // Generate two digits per iteration
    while (nb >= 65536) {
      qo = nb / 100;
      // really: r = i - (q * 100);
      re = nb - ((qo << 6) + (qo << 5) + (qo << 2));
      nb = qo;
      buf[--charPos] = DigitOnes[re];
      buf[--charPos] = DigitTens[re];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i <= 65536, i);
    for (;;) {
      qo = (nb * 52429) >>> (16 + 3);
      re = nb - ((qo << 3) + (qo << 1)); // r = i-(q*10) ...
      buf[--charPos] = digits[re];
      nb = qo;
      if (nb == 0) {
        break;
      }
    }

    if (minus) {
      buf[--charPos] = MINUS;
    }

    source.write(buf, charPos, 19 - charPos);
  }


  private void writeLong(long nb) throws IOException {
    if (nb == Long.MIN_VALUE) {
      source.write(longminValue, 0, longminValue.length);
      return;
    }
    long qo;
    int re;
    int charPos = 19;
    byte[] buf = numberbuf;
    boolean minus = false;

    if (nb < 0) {
      minus = true;
      nb = -nb;
    }

    // Get 2 digits/iteration using longs until quotient fits into an int
    while (nb > Integer.MAX_VALUE) {
      qo = nb / 100;
      // really: r = i - (q * 100);
      re = (int) (nb - ((qo << 6) + (qo << 5) + (qo << 2)));
      nb = qo;
      buf[--charPos] = DigitOnes[re];
      buf[--charPos] = DigitTens[re];
    }

    // Get 2 digits/iteration using ints
    int q2;
    int i2 = (int) nb;
    while (i2 >= 65536) {
      q2 = i2 / 100;
      // really: r = i2 - (q * 100);
      re = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
      i2 = q2;
      buf[--charPos] = DigitOnes[re];
      buf[--charPos] = DigitTens[re];
    }

    // Fall thru to fast mode for smaller numbers
    // assert(i2 <= 65536, i2);
    for (;;) {
      q2 = (i2 * 52429) >>> (16 + 3);
      re = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
      buf[--charPos] = digits[re];
      i2 = q2;
      if (i2 == 0) {
        break;
      }
    }

    if (minus) {
      buf[--charPos] = MINUS;
    }

    source.write(buf, charPos, 19 - charPos);
  }

  @Override
  public JsonGenerator writeKey(String name) {
	try {
      objectCheck(name);
	} catch (IOException ex) {
	  throw new JsonGenerationException(ex.getMessage(), ex);
	}
	key = true;
    return this;
  }

}
