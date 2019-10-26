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

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import net.gcolin.common.lang.NumberUtil;
import net.gcolin.rest.util.UrlEncoder;

/**
 * Converter String to NewCookie.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class NewCookieParamConverter extends AbstractCookieParamConverter<NewCookie>
    implements
      Converter<NewCookie> {

  private static final HeaderDelegate<Date> HD =
      RuntimeDelegate.getInstance().createHeaderDelegate(Date.class);

  /**
   * Create a NewCookieParamConverter.
   */
  public NewCookieParamConverter() {

    deserializer.put("Comment", (cookie, val) -> cookie.comment = val);
    deserializer.put("Max-Age",
        (cookie, val) -> cookie.maxAge = NumberUtil.parseInt(val, NewCookie.DEFAULT_MAX_AGE));
    deserializer.put("HttpOnly", (cookie, val) -> cookie.httpOnly = true);
    deserializer.put("Secure", (cookie, val) -> cookie.secure = true);
    deserializer.put("Expires", (cookie, val) -> cookie.expiry = HD.fromString(val));

    @SuppressWarnings("unchecked")
    CookieSerializer<NewCookie>[] serializer = new CookieSerializer[8];
    System.arraycopy(this.serializer, 0, serializer, 1, this.serializer.length);

    serializer[0] = (value, str) -> {
      if (value.getComment() != null) {
        str.append("; Comment=\"").append(UrlEncoder.DEFAULT.encode(value.getComment())).append('"');
      }
    };

    serializer[4] = (value, str) -> {
      if (value.getMaxAge() != NewCookie.DEFAULT_MAX_AGE) {
        str.append("; Max-Age=").append(value.getMaxAge());
      }
    };

    serializer[5] = (value, str) -> {
      if (value.isSecure()) {
        str.append("; Secure=true");
      }
    };

    serializer[6] = (value, str) -> {
      if (value.isHttpOnly()) {
        str.append("; HttpOnly=true");
      }
    };

    serializer[7] = (value, str) -> {
      if (value.getExpiry() != null) {
        str.append("; Expires=").append(HD.toString(value.getExpiry()));
      }
    };

    setSerializer(serializer);
  }

  @Override
  protected NewCookie build(MutableCookie cookie) {
    return cookie.getImmutableNewCookie();
  }

}
