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
 * A decoder for UTF-8
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see <a href="http://en.wikipedia.org/wiki/UTF-8">Wikipedia UTF-8</a>
 */
public class Utf8Decoder extends Decoder {

  public Utf8Decoder(InputStream in) {
    super(in);
  }

  @Override
  protected int convert(byte[] in, char[] out) {
    int ci = 0;
    int inSize = size;
    int bi = 0;
    for (; bi < inSize; bi++, ci++) {
      int r1 = in[bi];
      if ((r1 & 0x80) == 0) {
        out[ci] = (char) r1;
      } else {
        if ((r1 & 0xE0) == 0xC0) {
          if (bi >= inSize - 1) {
        	  throwBadEncoding();
          } else {
            out[ci] = (char) ((r1 & 0x1F) << 6 | (in[++bi] & 0x3f));
          }
        } else if ((r1 & 0xF0) == 0xE0) {
          if (bi >= inSize - 2) {
        	  throwBadEncoding();
          } else {
        	out[ci] = (char) ((r1 & 0xF) << 12 | (in[++bi] & 0x3f) << 6 | (in[++bi] & 0x3f));
          }
        } else if ((r1 & 0xF8) == 0xF0) {
          if (bi >= inSize - 3) {
        	  throwBadEncoding();
          } else {
            int point =  (r1 & 0x7) << 18 |  (in[++bi] & 0x3f) << 12 | (in[++bi] & 0x3f) << 6 | (in[++bi] & 0x3f);
            out[ci] = Character.highSurrogate(point);
            out[++ci] = Character.lowSurrogate(point);
          }
        } else {
          throwBadEncoding();
        }
      }
    }
    size = bi;
    return ci;
  }

}
