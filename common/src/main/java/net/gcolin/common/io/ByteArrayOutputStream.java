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
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * An unsynchronized ByteArrayOutputStream which use pooled byte arrays.
 *
 * <p>
 * Has the useful methods {@code writeTo(OutputStream)} and {@code writeTo(RandomAccessFile)}
 * </p>
 *
 * <p>
 * WARNING : to release all the byte, use {@code release()} to clear the data (release all bytes
 * expect one array), use {@code reset()}
 * </p>
 *
 * <p>
 * If the bytes are not released, they will be garbage collected with the ByteArrayOutputStream.
 * </p>
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class ByteArrayOutputStream extends OutputStream {

  private List<byte[]> all = new ArrayList<>();
  private byte[] current;
  private int index;

  public ByteArrayOutputStream() {
    enlarge();
  }

  private void enlarge() {
    current = Io.takeBytes();
    all.add(current);
    index = 0;
  }

  @Override
  public void write(int bt) throws IOException {
    if (current.length == index) {
      enlarge();
    }
    current[index++] = (byte) bt;
  }

  @Override
  public void write(byte[] array, int off, int len) throws IOException {
    if (current.length - index < len) {
      int rem = len;
      int offset = off;
      while (rem > 0) {
        int todo = Math.min(current.length - index, rem);
        System.arraycopy(array, offset, current, index, todo);
        offset += todo;
        rem -= todo;
        index += todo;
        if (rem > 0) {
          enlarge();
        }
      }
    } else {
      System.arraycopy(array, off, current, index, len);
      index += len;
    }
  }

  public int getSize() {
    return (all.size() - 1) * Io.BUFFER_SIZE + index;
  }

  public boolean isEmpty() {
    return index == 0 && all.size() == 1;
  }

  /**
   * Get the content of the output stream.
   * 
   * @return a byte array
   */
  public byte[] toByteArray() {
    byte[] tmp = new byte[getSize()];
    int off = 0;
    for (int i = 0; i < all.size() - 1; i++, off += Io.BUFFER_SIZE) {
      System.arraycopy(all.get(i), 0, tmp, off, Io.BUFFER_SIZE);
    }
    System.arraycopy(current, 0, tmp, off, index);
    return tmp;
  }

  /**
   * Write the data to an output stream.
   * 
   * @param out an output stream
   * @throws IOException if an I/O error occurs.
   */
  public void writeTo(OutputStream out) throws IOException {
    for (int i = 0; i < all.size() - 1; i++) {
      out.write(all.get(i), 0, Io.BUFFER_SIZE);
    }
    out.write(current, 0, index);
  }

  /**
   * Write the data to a random access file.
   * 
   * @param out a random access file
   * @throws IOException if an I/O error occurs.
   */
  public void writeTo(RandomAccessFile out) throws IOException {
    for (int i = 0; i < all.size() - 1; i++) {
      out.write(all.get(i), 0, Io.BUFFER_SIZE);
    }
    out.write(current, 0, index);
  }

  /**
   * Recycle the internal byte arrays.
   */
  public void release() {
    for (int i = 0; i < all.size(); i++) {
      Io.recycleBytes(all.get(i));
    }
    current = null;
  }

  /**
   * Clear the output stream for reusing it.
   */
  public void reset() {
    for (int i = all.size() - 1; i > 0; i--) {
      Io.recycleBytes(all.get(i));
      all.remove(i);
    }
    current = all.get(0);
    index = 0;
  }

}
