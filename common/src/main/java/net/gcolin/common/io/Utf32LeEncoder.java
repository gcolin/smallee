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
 * An encoder for UTF-32 little-endian
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see <a href="http://en.wikipedia.org/wiki/UTF-32">Wikipedia UTF-32</a>
 */
public class Utf32LeEncoder extends Utf32BeEncoder {

  private static final byte[] BOM = {(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};

  public Utf32LeEncoder(OutputStream out) {
    super(out);
  }

  @Override
  protected void put(int ptr, byte[] buf, int chr) {
    buf[ptr] = (byte) chr;
    buf[ptr + 1] = (byte) (chr >> 8);
    buf[ptr + 2] = (byte) (chr >> 16);
    buf[ptr + 3] = (byte) (chr >> 24);
  }

  @Override
  public void writeBom() throws IOException {
    out.write(BOM);
  }

}
