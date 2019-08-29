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

package net.gcolin.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * An encoder encodes characters to an output stream
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class Encoder {

  private static final Map<String, Function<OutputStream, Encoder>> ENCODER_FACTORY =
      new HashMap<>();
  protected byte[] tmp;
  private char[] cbuf = new char[1];
  protected OutputStream out;
  protected int max;

  static {
    ENCODER_FACTORY.put(StandardCharsets.UTF_8.name(), o -> new Utf8Encoder(o));
    ENCODER_FACTORY.put("utf8", ENCODER_FACTORY.get(StandardCharsets.UTF_8.name()));
    ENCODER_FACTORY.put("utf-8", ENCODER_FACTORY.get(StandardCharsets.UTF_8.name()));
    ENCODER_FACTORY.put("UTF_8", ENCODER_FACTORY.get(StandardCharsets.UTF_8.name()));
    ENCODER_FACTORY.put("UTF8", ENCODER_FACTORY.get(StandardCharsets.UTF_8.name()));

    ENCODER_FACTORY.put(StandardCharsets.UTF_16.name(), o -> new Utf16Encoder(o));
    ENCODER_FACTORY.put("utf16", ENCODER_FACTORY.get(StandardCharsets.UTF_16.name()));
    ENCODER_FACTORY.put("unicode", ENCODER_FACTORY.get(StandardCharsets.UTF_16.name()));
    ENCODER_FACTORY.put("ISO-10646-UCS-2", ENCODER_FACTORY.get(StandardCharsets.UTF_16.name()));


    ENCODER_FACTORY.put(StandardCharsets.UTF_16BE.name(), o -> new Utf16BeEncoder(o));
    ENCODER_FACTORY.put("UnicodeBig", ENCODER_FACTORY.get(StandardCharsets.UTF_16BE.name()));
    ENCODER_FACTORY.put("X-UTF-16BE", ENCODER_FACTORY.get(StandardCharsets.UTF_16BE.name()));
    ENCODER_FACTORY.put("UnicodeBigUnmarked",
        ENCODER_FACTORY.get(StandardCharsets.UTF_16BE.name()));

    ENCODER_FACTORY.put(StandardCharsets.UTF_16LE.name(), o -> new Utf16LeEncoder(o));
    ENCODER_FACTORY.put("X-UTF-16LE", ENCODER_FACTORY.get(StandardCharsets.UTF_16LE.name()));
    ENCODER_FACTORY.put("UnicodeLittleUnmarked",
        ENCODER_FACTORY.get(StandardCharsets.UTF_16LE.name()));

    String utf32 = "UTF_32";
    ENCODER_FACTORY.put(utf32, o -> new Utf32BeEncoder(o));
    ENCODER_FACTORY.put("UTF32", ENCODER_FACTORY.get(utf32));
    ENCODER_FACTORY.put("UTF-32", ENCODER_FACTORY.get(utf32));

    String utf32be = "UTF-32BE";
    ENCODER_FACTORY.put(utf32be, o -> new Utf32BeEncoder(o));
    ENCODER_FACTORY.put("UTF_32BE", ENCODER_FACTORY.get(utf32be));
    ENCODER_FACTORY.put("X-UTF-32BE", ENCODER_FACTORY.get(utf32be));
    ENCODER_FACTORY.put("UTF_32BE_BOM", ENCODER_FACTORY.get(utf32be));
    ENCODER_FACTORY.put("UTF-32BE-BOM", ENCODER_FACTORY.get(utf32be));

    String utf32le = "UTF_32LE";
    ENCODER_FACTORY.put(utf32le, o -> new Utf32LeEncoder(o));
    ENCODER_FACTORY.put("UTF-32LE", ENCODER_FACTORY.get(utf32le));
    ENCODER_FACTORY.put("X-UTF-32LE", ENCODER_FACTORY.get(utf32le));
    ENCODER_FACTORY.put("UTF_32LE_BOM", ENCODER_FACTORY.get(utf32le));
    ENCODER_FACTORY.put("UTF-32LE-BOM", ENCODER_FACTORY.get(utf32le));

    ENCODER_FACTORY.put(StandardCharsets.ISO_8859_1.name(), o -> new Iso88591Encoder(o));

    for (String s : new String[] {"iso-ir-100", "ISO_8859-1", "latin1", "l1", "IBM819", "cp819",
        "csISOLatin1", "819", "IBM-819", "ISO8859_1", "ISO_8859-1:1987", "ISO_8859_1", "8859_1",
        "ISO8859-1"}) {
      ENCODER_FACTORY.put(s, ENCODER_FACTORY.get(StandardCharsets.ISO_8859_1.name()));
    }

    ENCODER_FACTORY.put(StandardCharsets.US_ASCII.name(), o -> new AsciiEncoder(o));

    for (String s : new String[] {"iso-ir-6", "ANSI_X3.4-1986", "ISO_646.irv:1991", "ASCII",
        "ISO646-US", "us", "IBM367", "cp367", "csASCII", "default", "646", "iso_646.irv:1983",
        "ANSI_X3.4-1968", "ascii7"}) {
      ENCODER_FACTORY.put(s, ENCODER_FACTORY.get(StandardCharsets.US_ASCII.name()));
    }
  }

  /**
   * Create an encoder.
   * 
   * @param out the output
   */
  public Encoder(OutputStream out) {
    this.out = out;
    tmp = Io.takeBytes();
    max = getMax(tmp.length);
  }
  
  public void setOutputStream(OutputStream out) {
    this.out = out;
  }

  /**
   * Create an encoder.
   * 
   * @param out the output
   * @param charset the encoding
   * @return an encoder
   */
  public static Encoder createEncoder(OutputStream out, String charset) {
    Function<OutputStream, Encoder> factory = ENCODER_FACTORY.get(charset);
    if (factory == null) {
      throw new UnsupportedOperationException("charset " + charset + " not supported");
    }
    return factory.apply(out);
  }

  public static boolean has(String charset) {
    return ENCODER_FACTORY.containsKey(charset);
  }

  public void close() {
    Io.recycleBytes(tmp);
  }

  protected abstract int getMax(int bufSize);

  /**
   * Write a string.
   * 
   * @param str the data.
   * @param off the start offset in the data.
   * @param len the number of bytes to write.
   * @throws IOException if an I/O error occurs.
   */
  public void write(String str, int off, int len) throws IOException {
    write(str.toCharArray(), off, len);
  }
  
  public void write(String str) throws IOException {
    write(str.toCharArray(), 0, str.length());
  }
  
  /**
   * Write a char.
   * 
   * @param ch a char
   * @throws IOException if an I/O error occurs.
   */
  public void write(int ch) throws IOException {
    cbuf[0] = (char) ch;
    write(cbuf, 0, 1);
  }

  /**
   * Write chars.
   * 
   * @param str the data.
   * @param off the start offset in the data.
   * @param len the number of bytes to write.
   * @throws IOException if an I/O error occurs.
   */
  public abstract void write(char[] str, int off, int len) throws IOException;
  
  public void writeBom() throws IOException {
    throw new UnsupportedOperationException();
  }

  protected void throwBadEncoding() {
    throw new IllegalArgumentException("bad char encoding");
  }
}
