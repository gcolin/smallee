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

import java.io.InputStream;

/**
 * A decoder for ISO-88591
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Iso88591Decoder extends Decoder {

  public Iso88591Decoder(InputStream in) {
    super(in);
  }

  @Override
  protected int convert(byte[] in, char[] out) {
    int inSize = size;
    for (int i = 0; i < inSize; i++) {
      out[i] = (char) in[i];
    }
    return inSize;
  }

}
