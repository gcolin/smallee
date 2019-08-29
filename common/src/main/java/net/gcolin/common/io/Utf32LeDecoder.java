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
 * A decoder for UTF-32 little-endian
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see <a href="http://en.wikipedia.org/wiki/UTF-32">Wikipedia UTF-32</a>
 */
public class Utf32LeDecoder extends Decoder {

  public Utf32LeDecoder(InputStream in) {
    super(in);
  }

  protected int next(byte[] ba, int pi) {
    return ((ba[pi + 3] & 0xff) << 24) | ((ba[pi + 2] & 0xff) << 16) | ((ba[pi + 1] & 0xff) << 8)
        | (ba[pi] & 0xff);
  }

  @Override
  protected int convert(byte[] in, char[] out) {
    int ci = 0;
    int inSize = size;
    if (!end) {
      inSize = inSize - (inSize % 4);
    }
    int bi = 0;
    for (; bi < inSize; bi += 4, ci++) {
      int r2 = next(in, bi);
      if (Character.isBmpCodePoint(r2)) {
        out[ci] = (char) r2;
      } else {
        out[ci] = Character.highSurrogate(r2);
        out[++ci] = Character.lowSurrogate(r2);
      }
    }
    size = bi;
    return ci;
  }

}
