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

import net.gcolin.common.lang.NumberUtil;
import net.gcolin.common.lang.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.CacheControl;

/**
 * Converter String to CacheControl.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CacheControlParamConverter implements Converter<CacheControl> {

  @FunctionalInterface
  interface Deserializer {
    void deserialize(CacheControl cc, String name, String value);
  }

  @FunctionalInterface
  interface Serializer {
    void serialize(CacheControl cc, StringBuilder str);
  }

  private static final Serializer[] SERIALIZERS = new Serializer[9];
  private static final Map<String, Deserializer> DESERIALIZERS = new HashMap<>();
  private static final Deserializer DEFAULT_DESERIALIZER = new Deserializer() {

    @Override
    public void deserialize(CacheControl cc, String name, String value) {
      cc.getCacheExtension().put(name, value == null ? "" : value);
    }

  };

  private static void toCollection(String value, Collection<String> collection) {
    if (value != null) {
      for (String val : value.split(",")) {
        collection.add(val.trim());
      }
    }
  }

  private static class ExtensionSerializer implements Serializer {

    @Override
    public void serialize(CacheControl cc, StringBuilder str) {
      if (!cc.getCacheExtension().isEmpty()) {
        List<String> sortedKeys = new ArrayList<>(cc.getCacheExtension().keySet());
        Collections.sort(sortedKeys);
        for (String key : sortedKeys) {
          String value = cc.getCacheExtension().get(key);
          if (value.isEmpty()) {
            str.append(", ").append(key);
          } else if (value.indexOf(' ') != -1 || value.indexOf(',') != -1) {
            str.append(", ").append(key).append("=\"").append(value).append('"');
          } else {
            str.append(", ").append(key).append('=').append(value);
          }
        }
      }
    }
  }

  static {
    DESERIALIZERS.put("no-cache", (cc, name, value) -> cc.setNoCache(true));
    DESERIALIZERS.put("no-store", (cc, name, value) -> cc.setNoStore(true));
    DESERIALIZERS.put("max-age", (cc, name, value) -> cc.setMaxAge(NumberUtil.parseInt(value, -1)));
    DESERIALIZERS.put("no-transform", (cc, name, value) -> cc.setNoTransform(true));
    DESERIALIZERS.put("public", (cc, name, value) -> cc.setPrivate(false));
    DESERIALIZERS.put("private", (cc, name, value) -> {
      cc.setPrivate(true);
      toCollection(value, cc.getPrivateFields());
    });
    DESERIALIZERS.put("no-cache", (cc, name, value) -> {
      cc.setNoCache(true);
      toCollection(value, cc.getNoCacheFields());
    });
    DESERIALIZERS.put("must-revalidate", (cc, name, value) -> cc.setMustRevalidate(true));
    DESERIALIZERS.put("proxy-revalidate", (cc, name, value) -> cc.setProxyRevalidate(true));
    DESERIALIZERS.put("s-maxage",
        (cc, name, value) -> cc.setSMaxAge(NumberUtil.parseInt(value, -1)));

    SERIALIZERS[0] = (cc, sb) -> {
      if (cc.isPrivate()) {
        sb.append("private");
        if (!cc.getPrivateFields().isEmpty()) {
          sb.append("=\"").append(String.join(",", cc.getPrivateFields())).append('"');
        }
      } else {
        sb.append("public");
      }
    };

    SERIALIZERS[1] = (cc, sb) -> {
      if (cc.isNoCache()) {
        sb.append(", no-cache");
        if (!cc.getPrivateFields().isEmpty()) {
          sb.append("=\"").append(String.join(",", cc.getNoCacheFields())).append('"');
        }
      }
    };

    SERIALIZERS[2] = (cc, sb) -> {
      if (cc.isNoStore()) {
        sb.append(", no-store");
      }
    };

    SERIALIZERS[3] = (cc, sb) -> {
      if (cc.isNoTransform()) {
        sb.append(", no-transform");
      }
    };

    SERIALIZERS[4] = (cc, sb) -> {
      if (cc.isMustRevalidate()) {
        sb.append(", must-revalidate");
      }
    };

    SERIALIZERS[5] = (cc, sb) -> {
      if (cc.isMustRevalidate()) {
        sb.append(", proxy-revalidate");
      }
    };

    SERIALIZERS[6] = (cc, sb) -> {
      if (cc.getMaxAge() != -1) {
        sb.append(", max-age=").append(cc.getMaxAge());
      }
    };

    SERIALIZERS[7] = (cc, sb) -> {
      if (cc.getSMaxAge() != -1) {
        sb.append(", s-maxage=").append(cc.getSMaxAge());
      }
    };

    SERIALIZERS[8] = new ExtensionSerializer();
  }

  @Override
  public CacheControl fromString(String value) {
    CacheControl cc = new CacheControl();
    cc.setNoTransform(false);

    String name = null;
    int prec = 0;
    boolean in = false;
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (in) {
        if (ch == '"') {
          in = false;
        }
      } else if (ch == '"') {
        in = true;
      } else if (ch == '=') {
        name = Strings.substringTrimed(value, prec, i);
        prec = i + 1;
      } else if (ch == ',') {
        append(cc, value, prec, i, name);
        prec = i + 1;
        name = null;
      }
    }
    append(cc, value, prec, value.length(), name);
    return cc;
  }

  private void append(CacheControl cc, String value, int start, int end, String key) {
    String val = Strings.substringTrimed(value, start, end);
    String name = key;
    if (name == null) {
      name = val;
      val = null;
    }
    Deserializer deserializer = DESERIALIZERS.get(name);
    if (deserializer == null) {
      DEFAULT_DESERIALIZER.deserialize(cc, name, val);
    } else {
      deserializer.deserialize(cc, name, val);
    }
  }

  @Override
  public String toString(CacheControl value) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < SERIALIZERS.length; i++) {
      SERIALIZERS[i].serialize(value, sb);
    }
    return sb.toString();
  }

}
