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
import java.io.Reader;

/**
 * A string reader unsynchronized.
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class StringReader extends Reader {

  private String source;
  private int index = 0;

  public StringReader(String source) {
    this.source = source;
  }

  @Override
  public int read() {
    return index == source.length() ? -1 : source.charAt(index++);
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    if (index == source.length()) {
      return -1;
    }
    int read = Math.min(len, source.length() - index);
    source.getChars(index, index + read, cbuf, off);
    index += read;
    return read;
  }

  @Override
  public void close() throws IOException {}

}
