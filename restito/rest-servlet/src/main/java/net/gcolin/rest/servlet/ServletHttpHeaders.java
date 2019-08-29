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

package net.gcolin.rest.servlet;

import net.gcolin.common.collection.Collections2;
import net.gcolin.rest.AbstractHttpHeaders;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.util.HttpHeader;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * HttpHeaders from an HttpServletRequest.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServletHttpHeaders extends AbstractHttpHeaders {

  private HttpServletRequest request;
  private MultivaluedMap<String, String> headers;
  private Map<String, Cookie> cookies;
  private List<MediaType> accept;

  public ServletHttpHeaders(HttpServletRequest request) {
    this.request = request;
  }

  @Override
  public List<Locale> getAcceptableLanguages() {
    return Collections2.toList(request.getLocales());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public List<MediaType> getAcceptableMediaTypes() {
    if (accept == null) {
      accept =
          (List) Collections2.toList(FastMediaType.iterator(request.getHeader(HttpHeader.ACCEPT)));
    }
    return accept;
  }

  @Override
  public Map<String, Cookie> getCookies() {
    if (cookies == null) {
      cookies = new HashMap<String, Cookie>();
      javax.servlet.http.Cookie[] all = request.getCookies();
      for (int i = 0; i < all.length; i++) {
        javax.servlet.http.Cookie cookie = all[i];
        cookies.put(cookie.getName(), new Cookie(cookie.getName(), cookie.getValue(),
            cookie.getPath(), cookie.getDomain(), cookie.getVersion()));
      }
    }
    return cookies;
  }

  @Override
  public Locale getLanguage() {
    return request.getLocale();
  }

  @Override
  public MediaType getMediaType() {
    return FastMediaType.valueOf(request.getContentType());
  }

  @Override
  public MultivaluedMap<String, String> getRequestHeaders() {
    if (headers == null) {
      headers = new MultivaluedHashMap<String, String>();
      Enumeration<String> names = request.getHeaderNames();
      while (names.hasMoreElements()) {
        String name = names.nextElement();
        headers.put(name.toLowerCase(), Collections2.toList(request.getHeaders(name)));
      }
    }
    return headers;
  }

}
