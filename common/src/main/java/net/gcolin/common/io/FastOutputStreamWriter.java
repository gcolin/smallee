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
import java.io.Writer;

/**
 * Write an output stream Fast when the stream is small
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class FastOutputStreamWriter extends Writer {

  private Encoder encoder;
  private OutputStream out;

  public FastOutputStreamWriter(OutputStream out, String charset) {
    this.out = out;
    encoder = Encoder.createEncoder(out, charset);
  }

  public static boolean isCompatible(String charset) {
    return Encoder.has(charset);
  }

  public Encoder getEncoder() {
    return encoder;
  }

  @Override
  public void write(int ch) throws IOException {
    encoder.write(ch);
  }

  @Override
  public void write(String str, int off, int len) throws IOException {
    encoder.write(str, off, len);
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    encoder.write(cbuf, off, len);
  }

  @Override
  public void flush() throws IOException {
    out.flush();
  }

  /**
   * Close the encoder without closing the stream.
   */
  public void release() {
    if (encoder != null) {
      encoder.close();
      encoder = null;
    }
  }

  @Override
  public void close() throws IOException {
    release();
    out.close();
  }

}
