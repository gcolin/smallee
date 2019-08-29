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

import net.gcolin.common.collection.Func;
import net.gcolin.rest.util.HttpHeader;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * A partial Response implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class AbstractResponse extends Response {

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static final Function<Object, String> OBJECT_TOSTRING = o -> {
    if (o == null || o instanceof String) {
      return (String) o;
    }
    return RuntimeDelegate.getInstance().createHeaderDelegate((Class) o.getClass()).toString(o);
  };

  public static final Function<List<Object>, List<String>> LIST_OBJECT_TOSTRING = o -> {
    if (o == null) {
      return null;
    }
    return Func.map(o, x -> OBJECT_TOSTRING.apply(x));
  };
  private static final Map<String, Consumer<AbstractResponse>> CACHE_CLEANERS = new HashMap<>();

  private Set<Link> links;
  private Set<String> allowedMethods;
  private Map<String, NewCookie> newCookies;
  private int length = -2;

  static {
    CACHE_CLEANERS.put(HttpHeader.LINK, x -> x.links = null);
    CACHE_CLEANERS.put(HttpHeader.ALLOW, x -> x.allowedMethods = null);
    CACHE_CLEANERS.put(HttpHeader.SET_COOKIE, x -> x.newCookies = null);
    CACHE_CLEANERS.put(HttpHeader.CONTENT_LENGTH, x -> x.length = -2);
  }

  protected void clearCache(String name) {
    if (name == null) {
      for (Consumer<AbstractResponse> cc : CACHE_CLEANERS.values()) {
        cc.accept(this);
      }
    } else {
      Consumer<AbstractResponse> cc = CACHE_CLEANERS.get(name.toLowerCase());
      if (cc != null) {
        cc.accept(this);
      }
    }
  }

  @Override
  public int getLength() {
    if (length == -2) {
      Integer len = (Integer) getHeaders().getFirst(HttpHeader.CONTENT_LENGTH);
      length = len == null ? -1 : len;
    }
    return length;
  }

  @Override
  public MediaType getMediaType() {
    return (MediaType) getHeaders().getFirst(HttpHeader.CONTENT_TYPE);
  }

  @Override
  public Locale getLanguage() {
    return (Locale) getHeaders().getFirst(HttpHeader.CONTENT_LANGUAGE);
  }

  @Override
  public URI getLocation() {
    return (URI) getHeaders().getFirst(HttpHeader.LOCATION);
  }

  @Override
  public Date getDate() {
    return (Date) getHeaders().getFirst(HttpHeader.DATE);
  }

  @Override
  public Date getLastModified() {
    return (Date) getHeaders().getFirst(HttpHeader.LAST_MODIFIED);
  }

  @Override
  public Set<String> getAllowedMethods() {
    if (allowedMethods == null) {
      List<Object> allowedMethodstring = getHeaders().get(HttpHeader.ALLOW);
      if (allowedMethodstring != null) {
        allowedMethods = new HashSet<>();
        fillAllowed(allowedMethodstring);
      } else {
        allowedMethods = Collections.emptySet();
      }
    }
    return allowedMethods;
  }

  private void fillAllowed(List<Object> allowedMethodstring) {
    for (Object allowedMethod : allowedMethodstring) {
      for (String part : ((String) allowedMethod).split(",")) {
        allowedMethods.add(part.trim());
      }
    }
  }

  @Override
  public Map<String, NewCookie> getCookies() {
    if (newCookies == null) {
      newCookies = new HashMap<>();
      List<Object> headerStrings = getHeaders().get(HttpHeader.SET_COOKIE);
      if (headerStrings != null) {
        for (Object headerString : headerStrings) {
          NewCookie cookie = (NewCookie) headerString;
          newCookies.put(cookie.getName(), cookie);
        }
      }
    }
    return newCookies;
  }

  @Override
  public String getHeaderString(String name) {
    List<String> str = getStringHeaders().get(name);
    return str == null ? null : String.join(", ", str);
  }

  @Override
  public EntityTag getEntityTag() {
    return (EntityTag) getHeaders().getFirst(HttpHeader.ETAG);
  }

  @Override
  public Set<Link> getLinks() {
    if (links == null) {
      links = new HashSet<>();
      List<Object> linkString = getHeaders().get("link");
      if (linkString != null) {
        for (Object link : linkString) {
          links.add((Link) link);
        }
      }
    }
    return links;
  }

  @Override
  public StatusType getStatusInfo() {
    return Status.fromStatusCode(getStatus());
  }

  @Override
  public <T> T readEntity(Class<T> entityType) {
    return readEntity(entityType, null);
  }

  @Override
  public <T> T readEntity(GenericType<T> entityType) {
    return readEntity(entityType, null);
  }

  @Override
  public boolean hasLink(String relation) {
    return getLink(relation) != null;
  }

  @Override
  public Link getLink(String relation) {
    return Func.find(getLinks(), x -> relation.equals(x.getRel()));
  }

  @Override
  public Builder getLinkBuilder(String relation) {
    Link link = getLink(relation);
    return link == null ? null : Link.fromLink(link);
  }

}
