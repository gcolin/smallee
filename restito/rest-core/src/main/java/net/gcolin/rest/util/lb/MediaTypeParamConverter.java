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

package net.gcolin.rest.util.lb;

import net.gcolin.common.lang.Strings;
import net.gcolin.rest.FastMediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

/**
 * Converter String to MediaType.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MediaTypeParamConverter implements Converter<MediaType> {

  @Override
  public String toString(MediaType value) {
    StringBuilder str = new StringBuilder();
    str.append(value.getType()).append('/').append(value.getSubtype());
    if (!value.getParameters().isEmpty()) {
      for (Entry<String, String> params : value.getParameters().entrySet()) {
        str.append("; ").append(params.getKey()).append("=\"").append(params.getValue())
            .append('"');
      }
    }
    return str.toString();
  }

  private String getType(String value, int mp, int end) {
    return mp == -1
        ? Strings.substringTrimed(value, 0, end)
        : Strings.substringTrimed(value, 0, mp);
  }

  private String getSubType(String value, int mp, int end) {
    return mp == -1
        ? Strings.substringTrimed(value, 0, end)
        : Strings.substringTrimed(value, mp + 1, end);
  }

  @Override
  public MediaType fromString(String value) {
    int index = value.indexOf(';');
    int mp = value.indexOf('/');
    if (index == -1) {
      return new FastMediaType(value, getType(value, mp, value.length()),
          getSubType(value, mp, value.length()), null);
    } else {
      String p1 = getType(value, mp, index);
      String p2 = getSubType(value, mp, index);
      return new FastMediaType(value, p1, p2, getParameters(index, value));
    }
  }

  /**
   * Extract parameters from a text representation.
   * 
   * @param offset the start offset of parameters
   * @param value a media type text representation
   * @return a map of parameters
   */
  public static Map<String, String> getParameters(int offset, String value) {
    Map<String, String> map = new HashMap<>();
    int index = offset;
    int index2 = index;
    while (index2 != -1) {
      index = index2 + 1;
      index2 = value.indexOf(';', index);
      int split = value.indexOf('=', index);
      if (split != -1) {
        map.put(Strings.substringTrimed(value, index, split),
            Strings.substringTrimed(value, split + 1, index2 == -1 ? value.length() : index2));
      }
    }
    return map;
  }
}
