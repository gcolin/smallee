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
import java.io.Reader;

/**
 * Read an input stream with BOM Fast when the stream is small
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 */
public class FastInputStreamReader extends Reader {

  private InputStream input;
  private Decoder decoder;

  public FastInputStreamReader(InputStream input) {
    this(input, null);
  }

  /**
   * Create an input stream reader. The constructor should not be called directly, use
   * {@code IO.reader()}
   * 
   * @param input an input
   * @param charset an encoding
   */
  public FastInputStreamReader(InputStream input, String charset) {
    this.input = input;
    if (charset != null) {
      decoder = Decoder.createDecoder(input, charset, this);
    } else {
      decoder = new InitDecoder(this, input);
    }
  }

  public Decoder getDecoder() {
    return decoder;
  }

  @Override
  public int read() throws IOException {
    return decoder.read();
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    return decoder.read(cbuf, off, len);
  }

  /**
   * Close the decoder without closing the stream.
   */
  public void release() {
    if (decoder != null) {
      decoder.close();
      decoder = null;
    }
  }

  public void setDecoder(Decoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public void close() throws IOException {
    release();
    if (input != null) {
      input.close();
    }
  }

}
