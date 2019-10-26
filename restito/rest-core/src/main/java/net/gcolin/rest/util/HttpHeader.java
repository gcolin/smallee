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

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import net.gcolin.common.collection.Func;
import net.gcolin.common.lang.Locales;
import net.gcolin.common.lang.NumberUtil;
import net.gcolin.rest.FastMediaType;

/**
 * HttpHeaders in lower case.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class HttpHeader {

  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String ACCEPT = HttpHeaders.ACCEPT.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String ACCEPT_CHARSET = HttpHeaders.ACCEPT_CHARSET.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.3"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String ACCEPT_ENCODING = HttpHeaders.ACCEPT_ENCODING.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.4"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String ACCEPT_LANGUAGE = HttpHeaders.ACCEPT_LANGUAGE.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.7"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String ALLOW = HttpHeaders.ALLOW.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.8"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String AUTHORIZATION = HttpHeaders.AUTHORIZATION.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String CACHE_CONTROL = HttpHeaders.CACHE_CONTROL.toLowerCase();
  /**
   * See &lt;a href="http://tools.ietf.org/html/rfc2183"&gt;IETF RFC-2183&lt;/a&gt;.
   */
  public static final String CONTENT_DISPOSITION = HttpHeaders.CONTENT_DISPOSITION.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.11"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String CONTENT_ENCODING = HttpHeaders.CONTENT_ENCODING.toLowerCase();
  /**
   * See &lt;a href="http://tools.ietf.org/html/rfc2392"&gt;IETF RFC-2392&lt;/a&gt;.
   */
  public static final String CONTENT_ID = HttpHeaders.CONTENT_ID.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.12"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String CONTENT_LANGUAGE = HttpHeaders.CONTENT_LANGUAGE.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.13"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String CONTENT_LENGTH = HttpHeaders.CONTENT_LENGTH.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.14"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String CONTENT_LOCATION = HttpHeaders.CONTENT_LOCATION.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.18"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String DATE = HttpHeaders.DATE.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.19"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String ETAG = HttpHeaders.ETAG.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String EXPIRES = HttpHeaders.EXPIRES.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.23"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String HOST = HttpHeaders.HOST.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.24"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String IF_MATCH = HttpHeaders.IF_MATCH.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.25"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String IF_MODIFIED_SINCE = HttpHeaders.IF_MODIFIED_SINCE.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.26"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String IF_NONE_MATCH = HttpHeaders.IF_NONE_MATCH.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.28"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String IF_UNMODIFIED_SINCE = HttpHeaders.IF_UNMODIFIED_SINCE.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.29"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String LAST_MODIFIED = HttpHeaders.LAST_MODIFIED.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String LOCATION = HttpHeaders.LOCATION.toLowerCase();
  /**
   * See &lt;a href="http://tools.ietf.org/html/rfc5988#page-6"&gt;Web Linking (IETF RFC-5988)
   * documentation&lt;/a&gt;.
   */
  public static final String LINK = HttpHeaders.LINK.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.37"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String RETRY_AFTER = HttpHeaders.RETRY_AFTER.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.43"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String USER_AGENT = HttpHeaders.USER_AGENT.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.44"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String VARY = HttpHeaders.VARY.toLowerCase();
  /**
   * See &lt;a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.47"&gt;HTTP/1.1
   * documentation&lt;/a&gt;.
   */
  public static final String WWW_AUTHENTICATE = HttpHeaders.WWW_AUTHENTICATE.toLowerCase();
  /**
   * See &lt;a href="http://www.ietf.org/rfc/rfc2109.txt"&gt;IETF RFC 2109&lt;/a&gt;.
   */
  public static final String COOKIE = HttpHeaders.COOKIE.toLowerCase();
  /**
   * See &lt;a href="http://www.ietf.org/rfc/rfc2109.txt"&gt;IETF RFC 2109&lt;/a&gt;.
   */
  public static final String SET_COOKIE = HttpHeaders.SET_COOKIE.toLowerCase();

  public static final Map<String, Function<String, List<Object>>> CONVERTERS;
  public static final Map<String, String> LIST_HEADERS;

  static {
    Map<String, String> lh = new HashMap<>();

    lh.put(ACCEPT, ",");
    lh.put(ACCEPT_CHARSET, ",");
    lh.put(ACCEPT_ENCODING, ",");
    lh.put(ACCEPT_LANGUAGE, ",");
    lh.put(COOKIE, ";");

    LIST_HEADERS = Collections.unmodifiableMap(lh);

    Map<String, Function<String, List<Object>>> conv = new HashMap<>();
    conv.put(ACCEPT, x -> Func.map(Headers.parse(x), h -> FastMediaType.valueOf(h.getValue())));
    conv.put(ACCEPT_CHARSET, x -> Func.map(Headers.parse(x), h -> Charset.forName(h.getValue())));
    conv.put(ACCEPT_ENCODING, x -> Func.map(Headers.parse(x), Header::getValue));
    conv.put(ACCEPT_LANGUAGE,
        x -> Func.map(Headers.parse(x), h -> Locales.fromString(h.getValue())));
    conv.put(CACHE_CONTROL, x -> Arrays.asList(CacheControl.valueOf(x)));

    conv.put(CONTENT_LANGUAGE, x -> Arrays.asList(Locales.fromString(x)));
    conv.put(CONTENT_LENGTH, x -> Arrays.asList(NumberUtil.parseInt(x, -1)));
    conv.put(CONTENT_LOCATION, x -> Arrays.asList(URI.create(x)));
    conv.put(CONTENT_TYPE, x -> Arrays.asList(FastMediaType.valueOf(x)));

    HeaderDelegate<Date> hdDate = RuntimeDelegate.getInstance().createHeaderDelegate(Date.class);
    conv.put(DATE, x -> Arrays.asList(hdDate.fromString(x)));
    HeaderDelegate<EntityTag> hdEtag =
        RuntimeDelegate.getInstance().createHeaderDelegate(EntityTag.class);
    conv.put(ETAG, x -> Arrays.asList(hdEtag.fromString(x)));
    conv.put(EXPIRES, conv.get(DATE));
    conv.put(LAST_MODIFIED, conv.get(DATE));
    conv.put(LOCATION, conv.get(CONTENT_LOCATION));
    conv.put(LINK, x -> Arrays.asList(Link.valueOf(x)));
    conv.put(COOKIE, x -> Arrays.asList(Cookie.valueOf(x)));
    conv.put(SET_COOKIE, x -> Arrays.asList(NewCookie.valueOf(x)));

    CONVERTERS = Collections.unmodifiableMap(conv);
  }

  private HttpHeader() {}

}
