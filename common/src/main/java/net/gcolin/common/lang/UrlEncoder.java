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

package net.gcolin.common.lang;

/**
 * A fast URLEncoder
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class UrlEncoder {

  private final boolean[] noEncoding = new boolean[256];
  private final String[] encodingTable = new String[256];

  
  /**
   * Create a fast and modifiable UrlEncoder.
   */
  public UrlEncoder() {
    for (int i = 'a'; i <= 'z'; i++) {
      noEncoding[i] = true;
    }
    for (int i = 'A'; i <= 'Z'; i++) {
      noEncoding[i] = true;
    }
    for (int i = '0'; i <= '9'; i++) {
      noEncoding[i] = true;
    }
    noEncoding[' '] = true;
    noEncoding['-'] = true;
    noEncoding['_'] = true;
    noEncoding['.'] = true;
    noEncoding['*'] = true;

    for (int i = 0; i < 0x80; i++) {
      if (!noEncoding[i]) {
        String hex = Integer.toHexString(i).toUpperCase();
        encodingTable[i] = "%" + (hex.length() == 1 ? "0" : "") + hex;
      }
    }
    for (int i = 0x80; i <= 0xFF; i++) {
      if (!noEncoding[i]) {
        encodingTable[i] = "%" + Integer.toHexString(0xC0 | (i >> 6)).toUpperCase() + "%"
            + Integer.toHexString(0x80 | (i & 0x3F)).toUpperCase();
      }
    }
  }

  
  /**
   * Remove a char form the encoder.
   * 
   * @param ch a char that will be not encoded
   */
  public void remove(char ch) {
    if (ch <= 0xFF) {
      noEncoding[ch] = true;
    }
  }

  /**
   * Add a char in the encoder.
   * 
   * @param ch a char that will be encoded
   */
  public void add(char ch) {
    if (ch <= 0xFF) {
      noEncoding[ch] = false;
      if (ch < 0x80) {
        String hex = Integer.toHexString(ch).toUpperCase();
        encodingTable[ch] = "%" + (hex.length() == 1 ? "0" : "") + hex;
      } else {
        encodingTable[ch] = "%" + Integer.toHexString(0xC0 | (ch >> 6)).toUpperCase() + "%"
            + Integer.toHexString(0x80 | (ch & 0x3F)).toUpperCase();
      }
    }
  }

  /**
   * Fast URL encoding.
   * 
   * @param input A string to encode
   * @return An URL encoded string
   */
  public String encode(String input) {
    StringBuilder out = null;

    for (int i = 0; i < input.length(); i++) {
      int ch = input.charAt(i);
      if (ch < noEncoding.length) {
        if (noEncoding[ch]) {
          if (ch == ' ') {
            ch = '+';
            if (out == null) {
              out = new StringBuilder(input.length());
              if (i > 0) {
                out.append(input, 0, i);
              }
            }
          }
          if (out != null) {
            out.append((char) ch);
          }
        } else {
          if (out == null) {
            out = new StringBuilder(input.length());
            if (i > 0) {
              out.append(input, 0, i);
            }
          }
          out.append(encodingTable[ch]);
        }
      } else {
        if (out == null) {
          out = new StringBuilder(input.length());
          if (i > 0) {
            out.append(input, 0, i);
          }
        }
        if (ch > 2047) {
          if (55296 <= ch) {
            int uc = ((ch & 0x3FF) << 10 | (int) input.charAt(++i) & 0x3FF) + 65536;
            out.append('%');
            appendHex(0xF0 | uc >> 18, out);
            out.append('%');
            appendHex(0x80 | uc >> 12 & 0x3F, out);
            out.append('%');
            appendHex(0x80 | uc >> 6 & 0x3F, out);
            out.append('%');
            appendHex(0x80 | (uc & 0x3F), out);
            continue;
          }
          out.append('%');
          appendHex(0xE0 | (ch >> 12), out);
          out.append('%');
          appendHex(0x80 | (ch >> 6 & 0x3F), out);
        } else {
          out.append('%');
          appendHex(0xC0 | (ch >> 6), out);
        }
        out.append('%');
        appendHex(0x80 | (ch & 0x3F), out);
      }
    }

    return out != null ? out.toString() : input;
  }

  private void appendHex(int in, StringBuilder out) {
    out.append(toHexChar(in / 16)).append(toHexChar(in % 16));
  }

  private char toHexChar(int in) {
    if (in < 10) {
      return (char) (in + 48);
    } else {
      return (char) (in + 55);
    }
  }

}
