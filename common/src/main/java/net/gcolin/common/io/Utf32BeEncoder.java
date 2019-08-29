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

/**
 * An encoder for UTF-32 big-endian
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see <a href="http://en.wikipedia.org/wiki/UTF-32">Wikipedia UTF-32</a>
 */
public class Utf32BeEncoder extends UtfEncoder {

  private static final byte[] BOM = {(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};

  public static final int UCS4_MIN = 0x10000;

  public Utf32BeEncoder(OutputStream out) {
    super(out);
  }

  @Override
  public void write(char[] str, int off, int len) throws IOException {
    int ptr = 0;
    byte[] buf = this.tmp;
    int idx = off;
    int max = off + len;
    int mbuf = buf.length - 4;
    while (idx < max) {
      char chr = str[idx++];
      if (Character.isHighSurrogate(chr)) {
        char c2 = str[idx++];
        if (Character.isLowSurrogate(c2)) {
          int character = (((chr & 0x3ff) << 10) | (c2 & 0x3ff)) + UCS4_MIN;
          put(ptr, buf, character);
        } else {
          throwBadEncoding();
        }
      } else {
        put(ptr, buf, chr);
      }
      ptr += 4;

      if (ptr >= mbuf) {
        out.write(buf, 0, ptr);
        ptr = 0;
      }
    }

    out.write(buf, 0, ptr);
  }

  protected void put(int ptr, byte[] buf, int chr) {
    buf[ptr] = (byte) (chr >> 24);
    buf[ptr + 1] = (byte) (chr >> 16);
    buf[ptr + 2] = (byte) (chr >> 8);
    buf[ptr + 3] = (byte) chr;
  }

  @Override
  public void writeBom() throws IOException {
    out.write(BOM);
  }

}
