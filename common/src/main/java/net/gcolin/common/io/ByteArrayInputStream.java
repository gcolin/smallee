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

/**
 * An unsynchronized ByteArrayInputStream
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class ByteArrayInputStream extends InputStream {

  private byte[] array;
  private int index = 0;

  public ByteArrayInputStream(byte[] array) {
    this.array = array;
  }

  @Override
  public int read() throws IOException {
    return array.length == index ? -1 : array[index++] & 0xFF;
  }

  @Override
  public int read(byte[] ba, int off, int len) throws IOException {
    if (this.array.length == index) {
      return -1;
    }
    int nb = Math.min(len, this.array.length - index);
    System.arraycopy(this.array, index, ba, off, nb);
    index += nb;
    return nb;
  }

}
