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

package net.gcolin.rest;

import net.gcolin.common.lang.Strings;
import net.gcolin.common.lang.UrlEncoder;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

/**
 * The UriBuilder implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class UriBuilderImpl extends UriBuilder implements Cloneable {

  private static final UrlEncoder ENCODER = new UrlEncoder();
  private static final UrlEncoder ENCODER_WITH_SLASH = new UrlEncoder();
  private static final UrlEncoder ENCODER_USERINFO = new UrlEncoder();
  private static final String PREFIX = "//";
  private String scheme;
  private String authority;
  private String userInfo;
  private String host;
  private int port;
  private String path;
  private String query;
  private String fragment;
  private String ssp;
  private Map<String, String> vars = new HashMap<>();

  static {
    ENCODER.remove('{');
    ENCODER.remove('}');
    ENCODER.remove('/');

    ENCODER_WITH_SLASH.remove('{');
    ENCODER_WITH_SLASH.remove('}');

    ENCODER_USERINFO.remove('{');
    ENCODER_USERINFO.remove('}');
    ENCODER_USERINFO.remove(':');
  }

  public UriBuilderImpl() {}

  private UriBuilderImpl(UriBuilderImpl other) {
    path = other.path;
    query = other.query;
    this.ssp = other.ssp;
    this.authority = other.authority;
    this.fragment = other.fragment;
    this.host = other.host;
    this.port = other.port;
    this.scheme = other.scheme;
    this.userInfo = other.userInfo;
    vars.putAll(other.vars);
  }

  @Override
  public UriBuilder clone() {
    return new UriBuilderImpl(this);
  }

  @Override
  public UriBuilder uri(URI uri) {
    if (uri == null) {
      throw new IllegalArgumentException();
    }
    return uri(uri.toString());
  }

  @Override
  public UriBuilder uri(String uriTemplate) {
    if (uriTemplate == null) {
      throw new IllegalArgumentException();
    }
    int fragmentIndex = uriTemplate.indexOf('#');
    if (fragmentIndex != -1) {
      fragment = uriTemplate.substring(fragmentIndex + 1);
    } else {
      fragmentIndex = uriTemplate.length();
    }

    int schemeIndex = uriTemplate.indexOf(':');
    if (schemeIndex != -1 && !uriTemplate.matches("^\\w+:\\/\\/.*")) {
      // opaque
      scheme = uriTemplate.substring(0, schemeIndex);
      ssp = uriTemplate.substring(schemeIndex + 1, fragmentIndex);
      return this;
    }
    if (ssp != null && schemeIndex == -1 && !uriTemplate.startsWith(PREFIX)) {
      ssp = uriTemplate;
      return this;
    }
    ssp = null;
    int endAuthority;

    if (schemeIndex == -1) {
      if (uriTemplate.startsWith(PREFIX)) {
        schemeIndex = PREFIX.length();
      }
    } else {
      scheme = uriTemplate.substring(0, schemeIndex);
      schemeIndex++;
    }

    if (schemeIndex == -1) {
      endAuthority = 0;
    } else {
      while (uriTemplate.charAt(schemeIndex) == '/') {
        schemeIndex++;
      }

      endAuthority = smallIndex(fragmentIndex, uriTemplate.indexOf('/', schemeIndex),
          uriTemplate.indexOf('?', schemeIndex), uriTemplate.indexOf('#', schemeIndex));

      authority = uriTemplate.substring(schemeIndex, endAuthority);
      int atIndex = indexOfUnique('@', schemeIndex, endAuthority, uriTemplate);
      if (atIndex != -1) {
        userInfo = uriTemplate.substring(schemeIndex, atIndex);
        schemeIndex = atIndex + 1;
      }
      int portIndex = indexOfUnique(':', schemeIndex, endAuthority, uriTemplate);
      if (portIndex != -1) {
        port = Integer.parseInt(uriTemplate.substring(portIndex + 1, endAuthority));
        host = uriTemplate.substring(schemeIndex, portIndex);
      } else {
        host = uriTemplate.substring(schemeIndex, endAuthority);
      }
    }
    int queryIndex = uriTemplate.indexOf('?', endAuthority);
    if (queryIndex != -1) {
      query = uriTemplate.substring(queryIndex + 1, fragmentIndex);
      fragmentIndex = queryIndex;
    }
    if (endAuthority != fragmentIndex) {
      path = uriTemplate.substring(endAuthority, fragmentIndex);
    }
    return this;
  }

  private int indexOfUnique(char ch, int start, int end, String str) {
    int found = -1;
    for (int i = start; i < end; i++) {
      if (str.charAt(i) == ch) {
        if (found != -1) {
          return -1;
        } else {
          found = i;
        }
      }
    }
    return found;
  }

  private int smallIndex(int... integers) {
    int max = integers[0];
    for (int i = 1; i < integers.length; i++) {
      int elt = integers[i];
      if (max == -1) {
        max = elt;
      } else if (elt != -1 && elt < max) {
        max = elt;
      }
    }
    return max;
  }

  @Override
  public UriBuilder scheme(String scheme) {
    this.scheme = scheme;
    return this;
  }

  @Override
  public UriBuilder schemeSpecificPart(String ssp) {
    return uri(scheme != null ? scheme + ":" + ssp : ssp);
  }

  @Override
  public UriBuilder userInfo(String ui) {
    if (ui == null && this.userInfo != null && authority != null) {
      authority = authority.replace(this.userInfo + "@", "");
      if (authority.length() == 0) {
        authority = null;
      }
    }
    this.userInfo = ui;
    return this;
  }

  @Override
  public UriBuilder host(String nhost) {
    if (nhost == null && this.host != null && authority != null) {
      authority = authority.replace(this.host, "");
      if (authority.length() == 0) {
        authority = null;
      }
    }
    this.host = nhost;
    return this;
  }

  @Override
  public UriBuilder port(int port) {
    if (port < -1) {
      throw new IllegalArgumentException();
    }
    if (port == -1 && this.port > 0) {
      authority = authority.replace(":" + this.port, "");
      if (authority.length() == 0) {
        authority = null;
      }
    }
    this.port = port;
    return this;
  }

  @Override
  public UriBuilder replacePath(String path) {
    this.path = path;
    return this;
  }

  @Override
  public UriBuilder path(String path) {
    if (path == null) {
      throw new IllegalArgumentException();
    }
    if (!path.isEmpty()) {
      if (path.matches("^\\w*:.*")) {
        uri(path);
      } else {
        appendPath(path);
      }
    }
    return this;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public UriBuilder path(Class resource) {
    Path pa = Path.class.cast(resource.getAnnotation(Path.class));
    if (pa == null) {
      throw new IllegalArgumentException("PATH_ANNOTATION_MISSING in " + resource);
    }
    appendPath(pa.value());
    return this;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public UriBuilder path(Class resource, String methodName) {
    Method found = null;
    for (Method m : resource.getMethods()) {
      if (methodName.equals(m.getName())) {
        found = m;
        break;
      }
    }

    if (found == null) {
      throw new IllegalArgumentException("METHOD_NOT_FOUND " + methodName + " in " + resource);
    }

    return path(found);
  }

  @Override
  public UriBuilder path(Method method) {
    Path pa = method.getAnnotation(Path.class);
    if (pa == null) {
      throw new IllegalArgumentException("PATH_ANNOTATION_MISSING in " + method);
    }
    appendPath(pa.value());
    return this;
  }

  private void appendPath(String npath) {
    if (path == null) {
      path = "";
    }
    String ep = encodePath(npath);
    if (path.endsWith("/")) {
      if (ep.startsWith("/")) {
        path += ep.substring(1);
      } else {
        path += ep;
      }
    } else {
      if (ep.startsWith("/") || path.length() == 0 && authority == null) {
        path += ep;
      } else {
        path += "/" + ep;
      }
    }
  }

  private String encodePath(String npath) {
    StringBuilder str = new StringBuilder();
    boolean inVar = false;
    int prec = 0;
    for (int i = 0; i < npath.length(); i++) {
      char ch = npath.charAt(i);
      if (inVar && ch == '}') {
        inVar = false;
      } else if (ch == '{') {
        inVar = true;
      } else if (!inVar && ch == ' ') {
        str.append(npath.substring(prec, i)).append("%20");
        prec = i + 1;
      }
    }
    if (prec == 0) {
      return npath;
    }
    if (prec != npath.length()) {
      str.append(npath.substring(prec));
    }
    return str.toString();
  }

  @Override
  public UriBuilder segment(String... segments) {
    for (String segment : segments) {
      appendPath(ENCODER_WITH_SLASH.encode(segment));
    }
    return this;
  }

  @Override
  public UriBuilder replaceMatrix(String matrix) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder matrixParam(String name, Object... values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder replaceMatrixParam(String name, Object... values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UriBuilder replaceQuery(String query) {
    this.query = query;
    return this;
  }

  @Override
  public UriBuilder queryParam(String name, Object... values) {
    if (values == null) {
      return this;
    }
    if (query == null) {
      query = "";
    }
    String nameenc = ENCODER.encode(name);
    StringBuilder str = new StringBuilder(query);

    boolean hasNull = false;
    boolean hasNotNull = true;
    for (Object value : values) {
      if (value == null) {
        hasNull = true;
      } else {
        hasNotNull = true;
        if (str.length() > 0) {
          str.append('&');
        }
        str.append(nameenc);
        str.append('=').append(ENCODER.encode(value.toString()));
      }
      if (hasNull && hasNotNull) {
        throw new IllegalArgumentException();
      }
    }
    query = str.toString();
    return this;
  }

  @Override
  public UriBuilder replaceQueryParam(String name, Object... values) {
    int idx = 0;
    if (query == null) {
      query = "";
    }
    StringBuilder str = new StringBuilder(query);
    while ((idx = str.indexOf(name + "=", idx)) != -1) {
      if (idx > 0 && str.charAt(idx - 1) != '&') {
        continue;
      }
      int end = str.indexOf("&", idx);
      if (end == -1) {
        str.delete(idx, query.length());
      } else {
        str.delete(idx, end + 1);
      }
    }
    query = str.toString();
    return queryParam(name, values);
  }

  @Override
  public UriBuilder fragment(String fragment) {
    this.fragment = fragment;
    return this;
  }

  @Override
  public UriBuilder resolveTemplate(String name, Object value) {
    resolveTemplate(name, value, true, true);

    return this;
  }

  private UriBuilder resolveTemplate(String name, Object value, boolean encode,
      boolean encodeSlashInPath) {
    if (!vars.containsKey(name) && value != null) {
      String val = value.toString();
      if (encode && encodeSlashInPath) {
        val = ENCODER_WITH_SLASH.encode(val);
      } else if (encode) {
        val = ENCODER.encode(val);
      } else if (encodeSlashInPath) {
        val = val.replaceAll("\\/", "%2F");
      }
      vars.put(name, val);
    }
    return this;
  }

  @Override
  public UriBuilder resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
    resolveTemplate(name, value, false, encodeSlashInPath);
    return this;
  }

  @Override
  public UriBuilder resolveTemplateFromEncoded(String name, Object value) {
    resolveTemplate(name, value, false, false);
    return this;
  }

  @Override
  public UriBuilder resolveTemplates(Map<String, Object> templateValues) {
    resolveTemplates(templateValues, true, true);
    return this;
  }

  @Override
  public UriBuilder resolveTemplates(Map<String, Object> templateValues,
      boolean encodeSlashInPath) {
    resolveTemplates(templateValues, true, encodeSlashInPath);
    return this;
  }

  private UriBuilder resolveTemplates(Map<String, Object> templateValues, boolean encode,
      boolean encodeSlashInPath) {
    for (Entry<String, Object> e : templateValues.entrySet()) {
      resolveTemplate(e.getKey(), e.getValue(), encode, encodeSlashInPath);
    }
    return this;
  }

  @Override
  public UriBuilder resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
    resolveTemplates(templateValues, false, false);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public URI buildFromMap(Map<String, ?> values) {
    resolveTemplates((Map<String, Object>) values, true, true);
    return buildFromEncoded();
  }

  @SuppressWarnings("unchecked")
  @Override
  public URI buildFromMap(Map<String, ?> values, boolean encodeSlashInPath) {
    resolveTemplates((Map<String, Object>) values, false, encodeSlashInPath);
    return buildFromEncoded();
  }

  @SuppressWarnings("unchecked")
  @Override
  public URI buildFromEncodedMap(Map<String, ?> values) {
    resolveTemplates((Map<String, Object>) values, false, false);
    return buildFromEncoded();
  }

  @Override
  public URI build(Object... values) {
    Object[] args = new Object[values.length];
    for (int i = 0; i < values.length; i++) {
      args[i] = ENCODER_WITH_SLASH.encode(values[i].toString());
    }
    return buildFromEncoded(args);
  }



  @Override
  public URI build(Object[] values, boolean encodeSlashInPath) {
    if (encodeSlashInPath) {
      Object[] args = new Object[values.length];
      for (int i = 0; i < values.length; i++) {
        args[i] = ENCODER_WITH_SLASH.encode(values[i].toString());
      }
      return buildFromEncoded(args);
    } else {
      Object[] args = new Object[values.length];
      for (int i = 0; i < values.length; i++) {
        args[i] = ENCODER.encode(values[i].toString());
      }
      return buildFromEncoded(args);
    }
  }

  @Override
  public URI buildFromEncoded(Object... values) {
    String template = toTemplate();
    int prec = 0;
    boolean in = false;
    int vindex = 0;
    StringBuilder str = new StringBuilder();
    Map<String, Integer> indexes = new HashMap<>();
    for (int i = 0; i < template.length(); i++) {
      char ch = template.charAt(i);
      if (!in && ch == '{') {
        str.append(template.substring(prec, i));
        prec = i + 1;
        in = true;
      } else if (in && ch == '}') {
        String key = template.substring(prec, i);
        int sep = key.indexOf(':');
        String regExpr = null;
        if (sep != -1) {
          regExpr = key.substring(sep + 1).trim();
          key = key.substring(0, sep);
        }
        key = key.trim();
        Integer index = indexes.get(key);
        if (index == null) {
          index = vindex++;
          indexes.put(key, index);
        }
        String val = null;
        if (index >= values.length && vars.containsKey(key)) {
          val = vars.get(key);
        } else if (index < values.length && values[index] != null) {
          val = values[index].toString();
        }
        if (val == null) {
          val = "%7B" + key + "%7D";
        } else if (regExpr != null && !val.matches(regExpr)) {
          throw new IllegalArgumentException(key + " is not valid. " + val
              + " should be compatible with the expression " + regExpr);
        }
        str.append(val);

        in = false;
        prec = i + 1;
      }
    }
    if (prec != template.length()) {
      str.append(template.substring(prec));
    }
    try {
      return new URI(str.toString());
    } catch (URISyntaxException ex) {
      throw new UriBuilderException(ex);
    }
  }

  @Override
  public String toTemplate() {
    StringBuilder sb = new StringBuilder();

    if (scheme != null) {
      sb.append(encodePath(scheme)).append(':');
    }

    if (ssp != null) {
      sb.append(ssp);
    } else {
      appendUriPath(sb);
    }

    if (!Strings.isNullOrEmpty(fragment)) {
      sb.append('#').append(fragment);
    }

    return sb.toString();
  }

  private void appendUriPath(StringBuilder sb) {
    boolean hasAuthority = appendAuthority(sb);

    if (!Strings.isNullOrEmpty(path)) {
      if (sb.length() > 0 && path.charAt(0) != '/') {
        sb.append("/");
      }
      sb.append(path);
    } else if (needRootSlash(hasAuthority)) {
      // if has authority and query or fragment and no path value, we
      // need to append root '/' to the path
      // see URI RFC 3986 section 3.3
      sb.append("/");
    }

    if (!Strings.isNullOrEmpty(query)) {
      sb.append('?').append(query);
    }
  }

  private boolean needRootSlash(boolean hasAuthority) {
    return hasAuthority && (!Strings.isNullOrEmpty(query) || !Strings.isNullOrEmpty(fragment));
  }

  private boolean appendAuthority(StringBuilder sb) {
    boolean hasAuthority = false;
    if (userInfo != null || host != null || port > 0) {
      hasAuthority = true;
      sb.append(PREFIX);

      if (userInfo != null) {
        sb.append(ENCODER_USERINFO.encode(userInfo));
        sb.append('@');
      }

      if (host != null) {
        sb.append(host);
      }

      if (port > 0) {
        sb.append(':').append(port);
      }
    } else if (authority != null) {
      hasAuthority = true;
      sb.append(PREFIX).append(authority);
    }
    return hasAuthority;
  }

}
