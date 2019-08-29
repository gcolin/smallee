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
 * Decode BOM or use the decoder of the default Charset.
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 */
public class InitDecoder extends Decoder {

  private FastInputStreamReader fin;

  public InitDecoder(FastInputStreamReader fin, InputStream in) {
    super(in, false);
    this.fin = fin;
  }

  @Override
  protected int convert(byte[] in, char[] out) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int read() throws IOException {
    char[] ch = new char[1];
    if (read(ch, 0, 1) > 0) {
      return ch[0];
    } else {
      return -1;
    }
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    byte[] buf = new byte[4];
    int bomSize = in.read(buf, 0, 4);
    int bompos = 0;
    int i0 = buf[0] & 0xFF;
    int i1 = buf[1] & 0xFF;
    int i2 = buf[2] & 0xFF;
    int i3 = buf[3] & 0xFF;
    Decoder decoder = null;

    if (bomSize >= 3 && i0 == 0xEF && i1 == 0xBB && i2 == 0xBF) {
      decoder = new Utf8Decoder(in);
      bompos = 3;
    }
    if (decoder == null && bomSize == 4) {
      if (i0 == 0xFF && i1 == 0xFE && i2 == 0x00 && i3 == 0x00) {
        decoder = new Utf32LeDecoder(in);
        bompos = 4;
      } else if (i0 == 0x00 && i1 == 0x00 && i2 == 0xFE && i3 == 0xFF) {
        decoder = new Utf32BeDecoder(in);
        bompos = 4;
      }
    }
    if (decoder == null && bomSize >= 2) {
      if (i0 == 0xFF && i1 == 0xFE) {
        decoder = new Utf16LeDecoder(in);
        bompos = 2;
      } else if (i0 == 0xFE && i1 == 0xFF) {
        decoder = new Utf16BeDecoder(in);
        bompos = 2;
      }
    }

    // No BOM, just use JSON RFC's encoding algo to auto-detect
    if (decoder == null && bomSize >= 3) {
      if (i0 == 0x00 && i1 == 0x00 && i2 == 0x00) {
        decoder = new Utf32BeDecoder(in);
      } else if (i0 == 0x00 && i2 == 0x00) {
        decoder = new Utf16BeDecoder(in);
      }
    }

    if (decoder == null && bomSize == 4) {
      if (i3 == 0x00 && i1 == 0x00 && i2 == 0x00) {
        decoder = new Utf32LeDecoder(in);
      } else if (i1 == 0x00 && i3 == 0x00) {
        decoder = new Utf16LeDecoder(in);
      }
    }

    if (decoder == null) {
      decoder = new Utf8Decoder(in);
    }
    if (bomSize - bompos > 0) {
      decoder.externalFill(buf, bompos, bomSize - bompos);
    }
    fin.setDecoder(decoder);
    return decoder.read(cbuf, off, len);
  }
}
