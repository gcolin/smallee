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

import net.gcolin.common.lang.Locales;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.server.ServerResponse;
import net.gcolin.rest.util.HeaderObjectMap;
import net.gcolin.rest.util.HeaderPair;
import net.gcolin.rest.util.HttpHeader;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;

/**
 * A ResponseBuilder implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ResponseBuilderImpl extends ResponseBuilder implements Cloneable {

  private int status;
  private Object entity;
  private MultivaluedMap<String, Object> headers;
  private MultivaluedMap<String, String> stringheaders;
  private ServerInvocationContext context;
  private Annotation[] annotations;
  private Annotation[] entityAnnotations = new Annotation[0];

  /**
   * Create a response builder.
   * 
   * @param context the context of the request
   */
  public ResponseBuilderImpl(ServerInvocationContext context) {
    this.context = context;
    if (context != null && context.getResource() != null) {
      annotations = context.getResource().getAnnotations();
    } else {
      annotations = new Annotation[0];
    }

    HeaderPair pair = HeaderObjectMap.createHeaders();
    headers = pair.getKey();
    stringheaders = pair.getValue();
  }

  private ResponseBuilderImpl(ResponseBuilderImpl rb) {
    this(rb.context);
    headers.putAll(rb.headers);
    status = rb.status;
    entity = rb.entity;
  }

  @Override
  public Response build() {
    return new ServerResponse(entity, headers, stringheaders, context == null ? null : context.getOutputStream(), status,
        annotations, entityAnnotations);
  }

  @Override
  public ResponseBuilder cacheControl(CacheControl cacheControl) {
    return headerSingle(HttpHeader.CACHE_CONTROL, cacheControl);
  }

  @Override
  public ResponseBuilder clone() {    
    return new ResponseBuilderImpl(this);
  }

  @Override
  public ResponseBuilder contentLocation(URI uri) {
    return headerSingle(HttpHeader.CONTENT_LOCATION, uri);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public ResponseBuilder cookie(NewCookie... cookies) {
    if (cookies != null) {
      headers.addAll(HttpHeader.SET_COOKIE, (List) Arrays.asList(cookies));
    } else {
      headers.remove(HttpHeader.SET_COOKIE);
    }
    return this;
  }

  @Override
  public ResponseBuilder entity(Object entity) {
    this.entity = entity;
    return this;
  }

  @Override
  public ResponseBuilder entity(Object entity, Annotation[] newannotations) {
    this.entity = entity;
    entityAnnotations = newannotations;
    return this;
  }

  @Override
  public ResponseBuilder expires(Date expires) {
    return headerSingle(HttpHeader.EXPIRES, expires);
  }

  @Override
  public ResponseBuilder header(String name, Object value) {
    return header(name, value, false);
  }

  private ResponseBuilder header(String name, Object value, boolean single) {
    if (value != null) {
      if (single) {
        headers.putSingle(name, value);
      } else {
        headers.add(name, value);
      }
    } else {
      headers.remove(name);
    }
    return this;
  }

  @Override
  public ResponseBuilder language(String language) {
    return headerSingle(HttpHeader.CONTENT_LANGUAGE, Locales.fromString(language));
  }

  @Override
  public ResponseBuilder language(Locale language) {
    return headerSingle(HttpHeader.CONTENT_LANGUAGE, language);
  }

  @Override
  public ResponseBuilder lastModified(Date date) {
    return headerSingle(HttpHeader.LAST_MODIFIED, date);
  }

  @Override
  public ResponseBuilder location(URI location) {
    return headerSingle(HttpHeader.LOCATION, location == null ? null
        : location.isAbsolute() || context == null ? location : context.getUriInfo().getBaseUri().resolve(location));
  }

  @Override
  public ResponseBuilder status(int arg0) {
    this.status = arg0;
    return this;
  }

  @Override
  public ResponseBuilder tag(EntityTag tag) {
    return headerSingle(HttpHeader.ETAG, tag);
  }

  @Override
  public ResponseBuilder tag(String tag) {
    return tag(tag == null ? null : new EntityTag(tag));
  }

  @Override
  public ResponseBuilder type(MediaType type) {
    return headerSingle(HttpHeader.CONTENT_TYPE, type);
  }

  @Override
  public ResponseBuilder type(String type) {
    return type(type == null ? null : MediaType.valueOf(type));
  }

  @Override
  public ResponseBuilder variant(Variant variant) {
    if (variant == null) {
      type((MediaType) null);
      language((String) null);
      return encoding((String) null);
    } else {
      type(variant.getMediaType());
      language(variant.getLanguage());
      return encoding(variant.getEncoding());
    }
  }

  @Override
  public ResponseBuilder variants(List<Variant> variants) {
    if (variants == null) {
      headerSingle(HttpHeader.VARY, null);
      return this;
    }

    if (variants.isEmpty()) {
      return this;
    }

    MediaType accept = variants.get(0).getMediaType();
    boolean varyAccept = false;

    Locale acceptLanguage = variants.get(0).getLanguage();
    boolean varyAcceptLanguage = false;

    String acceptEncoding = variants.get(0).getEncoding();
    boolean varyAcceptEncoding = false;

    for (Variant v : variants) {
      varyAccept |= !varyAccept && vary(v.getMediaType(), accept);
      varyAcceptLanguage |= !varyAcceptLanguage && vary(v.getLanguage(), acceptLanguage);
      varyAcceptEncoding |= !varyAcceptEncoding && vary(v.getEncoding(), acceptEncoding);
    }

    StringBuilder vary = new StringBuilder();
    append(vary, varyAccept, HttpHeaders.ACCEPT);
    append(vary, varyAcceptLanguage, HttpHeaders.ACCEPT_LANGUAGE);
    append(vary, varyAcceptEncoding, HttpHeaders.ACCEPT_ENCODING);

    if (vary.length() > 0) {
      headerSingle(HttpHeader.VARY, vary.toString());
    }
    return this;
  }

  @Override
  public ResponseBuilder variants(Variant... variants) {
    return variants(variants == null ? null : Arrays.asList(variants));
  }

  private void append(StringBuilder sb, boolean vary, String varyHeader) {
    if (vary) {
      if (sb.length() > 0) {
        sb.append(',');
      }
      sb.append(varyHeader);
    }
  }

  private boolean vary(Object header, Object vary) {
    return header != null && !header.equals(vary);
  }

  @Override
  public ResponseBuilder allow(String... methods) {
    if (methods == null || methods.length == 1 && methods[0] == null) {
      return allow((Set<String>) null);
    } else {
      return allow(new HashSet<String>(Arrays.asList(methods)));
    }
  }

  @Override
  public ResponseBuilder allow(Set<String> methods) {
    if (methods == null || methods.isEmpty()) {
      return header(HttpHeader.ALLOW, null, true);
    }

    StringBuilder allow = new StringBuilder();
    for (String m : methods) {
      append(allow, true, m);
    }
    return header(HttpHeader.ALLOW, allow.toString(), true);
  }

  @Override
  public ResponseBuilder encoding(String encoding) {
    return headerSingle(HttpHeader.CONTENT_ENCODING, encoding);
  }

  @Override
  public ResponseBuilder replaceAll(MultivaluedMap<String, Object> headers) {
    if (headers != null) {
      this.headers.clear();
      this.headers.putAll(headers);
    }
    return this;
  }

  @Override
  public ResponseBuilder links(Link... links) {
    if (links != null) {
      for (Link link : links) {
        header(HttpHeader.LINK, link, false);
      }
    } else {
      header(HttpHeader.LINK, null, false);
    }
    return this;
  }

  @Override
  public ResponseBuilder link(URI uri, String rel) {
    return header(HttpHeader.LINK, Link.fromUri(uri).rel(rel).build(), false);
  }

  @Override
  public ResponseBuilder link(String uri, String rel) {
    return header(HttpHeader.LINK, Link.fromUri(uri).rel(rel).build(), false);
  }

  private ResponseBuilder headerSingle(String name, Object value) {
    return header(name, value, true);
  }
}
