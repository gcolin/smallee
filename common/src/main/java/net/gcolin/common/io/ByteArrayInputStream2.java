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
 * An unsynchronized ByteArrayInputStream which reads a matrix bytes
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class ByteArrayInputStream2 extends InputStream {

  private byte[][] matrix;
  private int aindex = 0;
  private int index = 0;

  public ByteArrayInputStream2(byte[][] matrix) {
    this.matrix = matrix;
  }

  @Override
  public int read() throws IOException {
    if (matrix[aindex].length == index) {
      if (aindex == matrix.length - 1) {
        return -1;
      } else {
        index = 0;
        aindex++;
      }
    }
    return matrix[aindex][index++] & 0xFF;
  }

  @Override
  public int read(byte[] array, int off, int len) throws IOException {
    if (this.matrix[aindex].length == index) {
      if (aindex == this.matrix.length - 1) {
        return -1;
      } else {
        index = 0;
        aindex++;
      }
    }
    int nb = Math.min(len, this.matrix[aindex].length - index);
    System.arraycopy(this.matrix[aindex], index, array, off, nb);
    index += nb;
    return nb;
  }

}
