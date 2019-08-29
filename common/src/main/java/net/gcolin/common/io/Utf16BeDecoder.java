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

import java.io.InputStream;

/**
 * A decoder for UTF-16 big-endian
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see <a href="http://en.wikipedia.org/wiki/UTF-16">Wikipedia UTF-16</a>
 */
public class Utf16BeDecoder extends Decoder {

  public Utf16BeDecoder(InputStream in) {
    super(in);
  }

  protected int next(byte[] ba, int pa) {
    return ((ba[pa] & 0xff) << 8) | (ba[pa + 1] & 0xff);
  }

  @Override
  protected int convert(byte[] in, char[] out) {
    int ci = 0;
    int inSize = size;
    if (!end) {
      inSize = inSize - (inSize % 2) - 2;
    }
    int bi = 0;
    for (; bi < inSize; bi += 2, ci++) {
      int r1 = next(in, bi);
      if (r1 >= 0xD800) {
        bi += 2;
        int r2 = next(in, bi);
        int cp = (r1 << 10) + r2 - 0x35FDC00;
        out[ci] = (char) (0xd800 | (((cp - 0x10000) >> 10) & 0x3ff));
        out[++ci] = (char) (0xdc00 | ((cp - 0x10000) & 0x3ff));
      } else {
        out[ci] = (char) r1;
      }
    }
    size = bi;
    return ci;
  }

}
