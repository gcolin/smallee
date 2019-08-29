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
 * An encoder for UTF-8
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see <a href="http://en.wikipedia.org/wiki/UTF-8">Wikipedia UTF-8</a>
 */
public class Utf8Encoder extends UtfEncoder {

  private static final int SURR1_FIRST = 0xD800;
  private static final int SURR2_FIRST = 0xDC00;
  private static final int SURR2_LAST = 0xDFFF;
  private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

  public Utf8Encoder(OutputStream out) {
    super(out);
  }

  @Override
  public void write(char[] str, int off, int len) throws IOException {
    int ptr = 0;
    byte[] buf = this.tmp;
    int mbuf = this.max;
    while (len > 0) {
      int nb = Math.min(mbuf, len);
      int lmax = off + nb;
      len -= nb;
      if (ptr >= mbuf) {
        out.write(buf, 0, ptr);
        ptr = 0;
      }
      while (off < lmax) {
        char chr = str[off++];
        if (chr < 0x80) {
          buf[ptr++] = (byte) chr;
        } else if (chr < 0x800) { // 2-byte
          buf[ptr++] = (byte) (0xC0 | (chr >> 6));
          buf[ptr++] = (byte) (0x80 | (chr & 0x3F));
        } else if (chr < SURR1_FIRST || chr > SURR2_LAST) { // 3 bytes
          buf[ptr++] = (byte) (0xE0 | (chr >> 12));
          buf[ptr++] = (byte) (0x80 | (chr >> 6 & 0x3F));
          buf[ptr++] = (byte) (0x80 | (chr & 0x3F));
        } else { // 4 bytes
          int uc = 0x10000 + ((chr - SURR1_FIRST) << 10) + (str[off++] - SURR2_FIRST);
          buf[ptr++] = (byte) (0xF0 | uc >> 18);
          buf[ptr++] = (byte) (0x80 | uc >> 12 & 0x3F);
          buf[ptr++] = (byte) (0x80 | uc >> 6 & 0x3F);
          buf[ptr++] = (byte) (0x80 | (uc & 0x3F));
        }
      }
    }

    out.write(buf, 0, ptr);

  }

  @Override
  public void writeBom() throws IOException {
    out.write(BOM);
  }

}
