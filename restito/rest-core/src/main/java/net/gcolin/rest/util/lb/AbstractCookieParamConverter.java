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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import net.gcolin.common.lang.NumberUtil;
import net.gcolin.common.lang.Strings;
import net.gcolin.rest.Logs;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class AbstractCookieParamConverter<T extends Cookie> {

  @FunctionalInterface
  interface CookieSerializer<T extends Cookie> {

    void serialize(T value, StringBuilder str);

  }

  @FunctionalInterface
  interface CookieDeserializer {

    void deserialize(MutableCookie cookie, String val);

  }

  @SuppressWarnings("unchecked")
  protected CookieSerializer<T>[] serializer = new CookieSerializer[3];
  protected final Map<String, CookieDeserializer> deserializer = new HashMap<>();

  /**
   * Create a AbstractCookieParamConverter.
   */
  public AbstractCookieParamConverter() {
    deserializer.put("Domain", (cookie, val) -> cookie.domain = val);
    deserializer.put("Path", (cookie, val) -> cookie.path = val);
    deserializer.put("Version",
        (cookie, val) -> cookie.version = NumberUtil.parseInt(val, Cookie.DEFAULT_VERSION));

    serializer[0] = (value, str) -> {
      if (value.getPath() != null) {
        str.append("; Path=").append(value.getPath());
      }
    };

    serializer[1] = (value, str) -> {
      if (value.getDomain() != null) {
        str.append("; Domain=").append(value.getDomain());
      }
    };

    serializer[2] = (value, str) -> {
      if (value.getVersion() != Cookie.DEFAULT_VERSION) {
        str.append("; Version=").append(value.getVersion());
      }
    };
  }

  static class MutableCookie {

    String name = null;
    String value = null;
    String path = null;
    String domain = null;
    int version = Cookie.DEFAULT_VERSION;
    String comment = null;
    int maxAge = NewCookie.DEFAULT_MAX_AGE;
    boolean secure = false;
    boolean httpOnly = true;
    Date expiry = null;

    public MutableCookie(String name, String value) {
      if (name == null) {
        throw new IllegalArgumentException("bad cookie : name is not defined");
      }
      this.name = name;
      this.value = value;
    }

    public NewCookie getImmutableNewCookie() {
      return new NewCookie(name, value, path, domain, version, comment, maxAge, expiry, secure,
          httpOnly);
    }

    public Cookie getImmutableCookie() {
      return new Cookie(name, value, path, domain, version);
    }
  }

  public void setSerializer(CookieSerializer<T>[] serializer) {
    this.serializer = serializer;
  }

  protected abstract T build(MutableCookie cookie);

  /**
   * Convert a String to a Cookie.
   * 
   * @param value a cookie value
   * @return a cookie object
   */
  public T fromString(String value) {
    int prec = 0;
    String name = null;
    MutableCookie cookie = null;
    boolean in = false;
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      if (in) {
        if (ch == '"') {
          in = false;
        }
      } else if (ch == '=') {
        name = Strings.substringTrimed(value, prec, i);
        prec = i + 1;
      } else if (ch == '"') {
        in = true;
      } else if (ch == ';') {
        cookie = append(value, prec, i, cookie, name);
        prec = i + 1;
      }
    }

    if (prec < value.length()) {
      cookie = append(value, prec, value.length(), cookie, name);
    }

    return cookie != null ? build(cookie) : null;
  }

  private MutableCookie append(String value, int prec, int idx, MutableCookie cookie, String name) {
    String val = Strings.substringTrimed(value, prec, idx);
    if (cookie == null) {
      return new MutableCookie(name, val);
    } else {
      CookieDeserializer cd = deserializer.get(name);
      if (cd == null) {
        Logs.LOG.warn("unknown parameter {}", name);
      } else {
        cd.deserialize(cookie, val);
      }
    }
    return cookie;
  }

  /**
   * Convert a cookie object to the cookie value.
   * 
   * @param value a cookie object
   * @return a string representing the cookie
   */
  public String toString(T value) {
    StringBuilder str = new StringBuilder();
    str.append(value.getName()).append('=').append(value.getValue());
    CookieSerializer<T>[] serializers = serializer;
    for (int i = 0; i < serializers.length; i++) {
      serializers[i].serialize(value, str);
    }
    return str.toString();
  }

}
