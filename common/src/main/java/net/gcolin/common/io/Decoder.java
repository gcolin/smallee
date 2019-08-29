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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A decoder decodes an input stream to characters
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class Decoder {

  interface DecoderFactory {
    Decoder create(InputStream in, FastInputStreamReader rd);
  }
  
  private static final Map<String, DecoderFactory> DECODER_FACTORY =
      new HashMap<>();
  protected InputStream in;
  protected byte[] tmp;
  protected char[] buf;
  protected int size;
  protected int bpos;
  protected int bsize;
  protected int tmpSize;
  protected boolean end;

  static {
    DECODER_FACTORY.put(StandardCharsets.UTF_8.name(), (in, fr) -> new Utf8Decoder(in));
    DECODER_FACTORY.put("utf8", DECODER_FACTORY.get(StandardCharsets.UTF_8.name()));
    DECODER_FACTORY.put("utf-8", DECODER_FACTORY.get(StandardCharsets.UTF_8.name()));
    DECODER_FACTORY.put("UTF8", DECODER_FACTORY.get(StandardCharsets.UTF_8.name()));

    DECODER_FACTORY.put(StandardCharsets.UTF_16.name(), (in, fr) -> new InitDecoder(fr, in));
    DECODER_FACTORY.put("utf16", DECODER_FACTORY.get(StandardCharsets.UTF_16.name()));
    DECODER_FACTORY.put("unicode", DECODER_FACTORY.get(StandardCharsets.UTF_16.name()));
    DECODER_FACTORY.put("ISO-10646-UCS-2", DECODER_FACTORY.get(StandardCharsets.UTF_16.name()));

    DECODER_FACTORY.put(StandardCharsets.UTF_16BE.name(), (in, fr) -> new Utf16BeDecoder(in));
    DECODER_FACTORY.put("UnicodeBig", DECODER_FACTORY.get(StandardCharsets.UTF_16BE.name()));
    DECODER_FACTORY.put("X-UTF-16BE", DECODER_FACTORY.get(StandardCharsets.UTF_16BE.name()));
    DECODER_FACTORY.put("UnicodeBigUnmarked",
        DECODER_FACTORY.get(StandardCharsets.UTF_16BE.name()));

    DECODER_FACTORY.put(StandardCharsets.UTF_16LE.name(), (in, fr) -> new Utf16LeDecoder(in));
    DECODER_FACTORY.put("X-UTF-16LE", DECODER_FACTORY.get(StandardCharsets.UTF_16LE.name()));
    DECODER_FACTORY.put("UnicodeLittleUnmarked",
        DECODER_FACTORY.get(StandardCharsets.UTF_16LE.name()));

    String utf32 = "UTF_32";
    DECODER_FACTORY.put(utf32, (in, fr) -> new Utf32BeDecoder(in));
    DECODER_FACTORY.put("UTF32", DECODER_FACTORY.get(utf32));
    DECODER_FACTORY.put("UTF-32", DECODER_FACTORY.get(utf32));
    DECODER_FACTORY.put("UTF_32BE", DECODER_FACTORY.get(utf32));
    DECODER_FACTORY.put("X-UTF-32BE", DECODER_FACTORY.get(utf32));
    DECODER_FACTORY.put("UTF_32BE_BOM", DECODER_FACTORY.get(utf32));
    DECODER_FACTORY.put("UTF-32BE-BOM", DECODER_FACTORY.get(utf32));

    String utf32le = "UTF_32LE";
    DECODER_FACTORY.put(utf32le, (in, fr) -> new Utf32LeDecoder(in));
    DECODER_FACTORY.put("X-UTF-32LE", DECODER_FACTORY.get(utf32le));
    DECODER_FACTORY.put("UTF-32LE", DECODER_FACTORY.get(utf32le));
    DECODER_FACTORY.put("UTF_32LE_BOM", DECODER_FACTORY.get(utf32le));
    DECODER_FACTORY.put("UTF-32LE-BOM", DECODER_FACTORY.get(utf32le));

    DECODER_FACTORY.put(StandardCharsets.ISO_8859_1.name(), (in, fr) -> new Iso88591Decoder(in));

    for (String s : new String[] {"iso-ir-100", "ISO_8859-1", "latin1", "l1", "IBM819", "cp819",
        "csISOLatin1", "819", "IBM-819", "ISO8859_1", "ISO_8859-1:1987", "ISO_8859_1", "8859_1",
        "ISO8859-1"}) {
      DECODER_FACTORY.put(s, DECODER_FACTORY.get(StandardCharsets.ISO_8859_1.name()));
    }

    DECODER_FACTORY.put(StandardCharsets.US_ASCII.name(), (in, fr) -> new AsciiDecoder(in));

    for (String charset : new String[] {"iso-ir-6", "ANSI_X3.4-1986", "ISO_646.irv:1991", "ASCII",
        "ISO646-US", "us", "IBM367", "cp367", "csASCII", "default", "646", "iso_646.irv:1983",
        "ANSI_X3.4-1968", "ascii7"}) {
      DECODER_FACTORY.put(charset, DECODER_FACTORY.get(StandardCharsets.US_ASCII.name()));
    }
  }

  public Decoder(InputStream in) {
    this(in, true);
  }

  /**
   * Create a decoder.
   * 
   * @param in an input stream
   * @param needBuffer true if the decoder need a buffer
   */
  public Decoder(InputStream in, boolean needBuffer) {
    this.in = in;
    if (needBuffer) {
      tmp = Io.takeBytes();
      buf = Io.takeChars();
    }
  }

  /**
   * Create a decoder.
   * 
   * @param in an input stream
   * @param charset a charset
   * @param fr a parent reader
   * @return a decoder for the reader
   */
  public static Decoder createDecoder(InputStream in, String charset, FastInputStreamReader fr) {
    DecoderFactory factory = DECODER_FACTORY.get(charset);
    if (factory == null) {
      throw new UnsupportedOperationException("charset " + charset + " not supported");
    }
    return factory.create(in, fr);
  }

  public static boolean has(String charset) {
    return DECODER_FACTORY.containsKey(charset);
  }

  /**
   * Release the buffer.
   */
  public void close() {
    if (tmp != null) {
      Io.recycleBytes(tmp);
      Io.recycleChars(buf);
    }
  }

  protected void throwBadEncoding() {
    throw new IllegalArgumentException("bad char encoding");
  }

  protected abstract int convert(byte[] in, char[] out);

  private void fillBuffer() throws IOException {
    int rest = tmpSize - size;
    if (rest != 0) {
      System.arraycopy(tmp, size, tmp, 0, rest);
    }
    int nb = in.read(tmp, rest, tmp.length - rest);
    size = nb == -1 ? rest : nb + rest;
    tmpSize = size;
    end = nb == -1;
    bsize = convert(tmp, buf);
    bpos = 0;
  }

  /**
   * Erase the current buffer and fill it with the given array.
   * 
   * <p>Used when the initial decoder cannot find the BOM</p>
   *
   * @param array the data
   * @param start the start offset in the data.
   * @param len the number of bytes to write
   */
  public void externalFill(byte[] array, int start, int len) {
    tmpSize = len;
    size = 0;
    System.arraycopy(array, start, tmp, 0, len);
  }

  /**
   * Read a char.
   * 
   * @return a char or -1 if there is no more data.
   * @throws IOException if an I/O error occurs.
   */
  public int read() throws IOException {
    if (bpos == bsize) {
      if (end) {
        return -1;
      } else {
        fillBuffer();
        return read();
      }
    }
    return buf[bpos++];
  }

  /**
   * Read chars.
   * 
   * @param cbuf the array to write data
   * @param off the start offset in the data.
   * @param len the number of bytes to write
   * @return the number of char written or -1 if there is no more char.
   * @throws IOException if an I/O error occurs.
   */
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (bpos == bsize) {
      if (end) {
        return -1;
      } else {
        fillBuffer();
      }
    }

    int rem = len;
    int offset = off;
    while (rem > 0) {
      int nb = Math.min(rem, bsize - bpos);
      System.arraycopy(buf, bpos, cbuf, offset, nb);
      bpos += nb;
      rem -= nb;
      offset += nb;
      if (rem > 0) {
        if (end) {
          return len - rem;
        } else {
          fillBuffer();
        }
      }
    }
    return len;
  }

}
