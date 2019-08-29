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
 * An encoder for UTF-16 with BOM
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see <a href="http://en.wikipedia.org/wiki/UTF-16">Wikipedia UTF-16</a>
 */
public class Utf16Encoder extends Utf16BeEncoder {

  /**
   * Create a Utf16Encoder.
   * 
   * @param out a stream
   */
  public Utf16Encoder(OutputStream out) {
    super(out);
    try {
      writeBom();
    } catch (IOException ex) {
      throw new IllegalArgumentException(ex);
    }
  }

}
