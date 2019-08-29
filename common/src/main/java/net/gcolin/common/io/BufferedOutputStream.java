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
 * A BufferedOutputStream which use pooled byte array
 *
 * <p>Release the byte array on close</p>
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class BufferedOutputStream extends OutputStream {

  private byte[] buf;
  private int index = 0;
  private OutputStream delegate;

  public BufferedOutputStream(OutputStream delegate) {
    this.delegate = delegate;
    buf = Io.takeBytes();
  }

  @Override
  public void close() throws IOException {
    if (buf != null) {
      flush();
      delegate.close();
      Io.recycleBytes(buf);
      buf = null;
    }
  }

  @Override
  public void write(int bt) throws IOException {
    buf[index++] = (byte) bt;
    if (index == buf.length) {
      flush();
    }
  }

  @Override
  public void write(byte[] array, int off, int len) throws IOException {
    if (buf.length - index >= len) {
      System.arraycopy(array, off, buf, index, len);
      index += len;
      if (index == buf.length) {
        flush();
      }
    } else {
      int rem = len;
      int offset = off;
      while (rem > 0) {
        int rlen = Math.min(rem, buf.length - index);
        System.arraycopy(array, offset, buf, index, rlen);
        rem -= rlen;
        offset += rlen;
        index += rlen;
        if (index == buf.length) {
          flush();
        }
      }
    }
  }
  
  @Override
  public void flush() throws IOException {
    if (index > 0) {
      delegate.write(buf, 0, index);
      index = 0;
    }
  }

}
