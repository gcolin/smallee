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
 * An encoder writes char as byte
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class SimpleEncoder extends Encoder {

  public SimpleEncoder(OutputStream out) {
    super(out);
  }

  @Override
  protected int getMax(int bufSize) {
    return bufSize;
  }

  protected abstract boolean check(char ch);

  @Override
  public void write(char[] str, int off, int len) throws IOException {
    int ptr = 0;
    byte[] buf = this.tmp;
    int idx = off;
    int max = off + len;
    int mbuf = buf.length;
    while (idx < max) {
      char chr = str[idx++];
      if (check(chr)) {
        buf[ptr++] = (byte) chr;
      } else {
        throwBadEncoding();
      }

      if (ptr >= mbuf) {
        out.write(buf, 0, ptr);
        ptr = 0;
      }
    }
    out.write(buf, 0, ptr);
  }

}
