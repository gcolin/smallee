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

package net.gcolin.rest.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.gcolin.common.lang.Strings;

/**
 * The {@code Headers} class parses header from {@code String} to {@code Header}
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see Header
 */
public class Headers {

  private static final Comparator<Header> HEADER_COMPARATOR = (a1, a2) -> {
    float delta = a1.getSort() - a2.getSort();
    return Float.floatToRawIntBits(delta) == 0 ? 0 : delta > 0 ? -1 : 1;
  };

  private Headers() {}

  /**
   * Extract the parameters of a header.
   * 
   * @param header an HTTP header
   * @return the parameters
   */
  public static Map<String, String> getParameters(String header) {
    Map<String, String> params = new HashMap<>();
    int start = header.indexOf(';');
    if (start == -1) {
      return params;
    }

    String precName = null;
    int prec = start + 1;
    for (int i = start + 1; i < header.length(); i++) {
      char ch = header.charAt(i);
      if (ch == '=') {
        precName = Strings.decodeUrl(Strings.substringTrimed(header, prec, i)).toLowerCase();
        prec = i + 1;
      } else if (ch == ';') {
        params.put(precName, Strings.decodeUrl(Strings.substringTrimed(header, prec, i)));
        precName = null;
        prec = i + 1;
      }
    }
    if (precName != null) {
      params.put(precName,
          Strings.decodeUrl(Strings.substringTrimed(header, prec, header.length())));
    }
    return params;
  }

  /**
   * Extract the headers.
   *
   * <p>In the case of cookies for example, many headers are combined in one header 
   * separated with ,</p>
   * 
   * @param header an HTTP header
   * @return the headers
   */
  public static List<Header> parse(String header) {
    List<Header> list = new ArrayList<>();
    String name = null;
    String prop = null;
    float qv = 1.0f;
    int prec = 0;
    Map<String, String> params = new LinkedHashMap<>();
    for (int i = 0; i < header.length(); i++) {
      char ch = header.charAt(i);
      if (ch == '=') {
        prop = Strings.decodeUrl(Strings.substringTrimed(header, prec, i));
        prec = i + 1;
      } else if (isSep(ch)) {
        if (prop == null || name == null) {
          name = Strings.substringTrimed(header, prec, i);
        } else if (isQ(prop)) {
          qv = Float.parseFloat(Strings.substringTrimed(header, prec, i));
        } else {
          params.put(prop, Strings.decodeUrl(Strings.substringTrimed(header, prec, i)));
        }
        prop = null;
        if (ch == ',') {
          list.add(new Header(qv, name, params));
          name = null;
          params = new LinkedHashMap<>();
          qv = 1.0f;
        }
        prec = i + 1;
      }
    }
    if (prop == null) {
      name = Strings.substringTrimed(header, prec, header.length());
    } else if (isQ(prop)) {
      qv = Float.parseFloat(Strings.substringTrimed(header, prec, header.length()));
    } else {
      params.put(prop, Strings.decodeUrl(Strings.substringTrimed(header, prec, header.length())));
    }
    list.add(new Header(qv, name, params));
    Collections.sort(list, HEADER_COMPARATOR);
    return list;
  }

  private static boolean isSep(char ch) {
    return ch == ',' || ch == ';';
  }

  private static boolean isQ(String prop) {
    return prop.length() == 1 && prop.charAt(0) == 'q';
  }

}
