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

package net.gcolin.json;

/**
 * The {@code CharEncode} class encodes characters to JSON.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CharEncode {

  private static String[] CONVERT = new String[93];

  private static char[] UNESCAPE = new char[117];

  static {
    CONVERT['"'] = "\\\"";
    CONVERT['\\'] = "\\\\";
    CONVERT['\b'] = "\\b";
    CONVERT['\f'] = "\\f";
    CONVERT['\n'] = "\\n";
    CONVERT['\r'] = "\\r";
    CONVERT['\t'] = "\\t";

    UNESCAPE['b'] = '\b';
    UNESCAPE['t'] = '\t';
    UNESCAPE['n'] = '\n';
    UNESCAPE['f'] = '\f';
    UNESCAPE['r'] = '\r';
    UNESCAPE['"'] = '"';
    UNESCAPE['\\'] = '\\';
    UNESCAPE['/'] = '/';
  }

  private CharEncode() {}

  /**
   * Convert a char from ASCII to JSON.
   * 
   * @param ch a char
   * @return a JSON encoded char
   */
  public static String escape(char ch) {
    if (CONVERT.length > ch) {
      String str = CONVERT[ch];
      if (str != null) {
        return str;
      }
    }
    String hex = Integer.toHexString(ch);
    if (hex.length() >= 4) {
      return "\\u" + hex.substring(hex.length() - 4);
    } else {
      StringBuilder str = new StringBuilder("\\u");
      for (int i = hex.length(); i < 4; i++) {
        str.append('0');
      }
      str.append(hex);
      return str.toString();
    }
  }

  /**
   * Convert a char from JSON to ASCII.
   * 
   * @param ch a JSON encoded char
   * @return a decoded JSON char
   */
  public static char unescape(char ch) {
    if (UNESCAPE.length > ch) {
      return UNESCAPE[ch];
    }
    return '\0';
  }

}
