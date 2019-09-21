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
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * A StringWriter which uses char arrays
 * 
 * <p>
 * WARNING : Clear the data on {@code close()}
 * </p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class StringWriter extends Writer {

  private List<char[]> all = new ArrayList<>();
  private char[] current;
  private int index;

  public StringWriter() {
    enlarge();
  }

  private void enlarge() {
    current = Io.takeChars();
    all.add(current);
    index = 0;
  }

  @Override
  public void write(int ch) {
    if (current.length == index) {
      enlarge();
    }
    current[index++] = (char) ch;
  }

  @Override
  public void write(String cbuf, int off, int len) {
    if (current.length - index < len) {
      int rem = len;
      int offset = off;
      while (rem > 0) {
        int todo = Math.min(current.length - index, rem);
        cbuf.getChars(offset, todo + offset, current, index);
        offset += todo;
        rem -= todo;
        index += todo;
        if (rem > 0) {
          enlarge();
        }
      }
    } else {
      cbuf.getChars(off, len + off, current, index);
      index += len;
    }
  }

  @Override
  public void write(char[] cbuf, int off, int len) {
    if (current.length - index < len) {
      int rem = len;
      int offset = off;
      while (rem > 0) {
        int todo = Math.min(current.length - index, rem);
        System.arraycopy(cbuf, offset, current, index, todo);
        offset += todo;
        rem -= todo;
        index += todo;
        if (rem > 0) {
          enlarge();
        }
      }
    } else {
      System.arraycopy(cbuf, off, current, index, len);
      index += len;
    }
  }

  @Override
  public void flush() {
    // nothing
  }

  @Override
  public void close() {
    for (int i = 0; i < all.size(); i++) {
      Io.recycleChars(all.get(i));
    }
    all.clear();
    current = null;
  }

  /**
   * Reset the writer for another use.
   */
  public void reset() {
    for (int i = all.size() - 1; i > 0; i--) {
      Io.recycleChars(all.get(i));
      all.remove(i);
    }
    index = 0;
    current = all.get(0);
  }

  public int getSize() {
    return (all.size() - 1) * Io.BUFFER_SIZE + index;
  }

  public boolean isEmpty() {
    return index == 0 && all.size() == 1;
  }

  @Override
  public String toString() {
    char[] ba = new char[getSize()];
    int off = 0;
    for (int i = 0; i < all.size() - 1; i++, off += Io.BUFFER_SIZE) {
      System.arraycopy(all.get(i), 0, ba, off, Io.BUFFER_SIZE);
    }
    System.arraycopy(current, 0, ba, off, index);
    return new String(ba);
  }

  /**
   * Write to a writer.
   * 
   * @param out a writer
   * @throws IOException if an I/O error occurs.
   */
  public void writeTo(Writer out) throws IOException {
    for (int i = 0; i < all.size() - 1; i++) {
      out.write(all.get(i), 0, Io.BUFFER_SIZE);
    }
    out.write(current, 0, index);
  }

}
