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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * A Link.Builder implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LinkBuilder implements Builder {

  private URI baseUri;
  private UriBuilder uriBuilder;
  private Map<String, String> params = new HashMap<>();

  @Override
  public Builder link(Link link) {
    uriBuilder = UriBuilder.fromUri(link.getUri());
    params.clear();
    params.putAll(link.getParams());
    baseUri = null;
    return this;
  }

  @Override
  public Builder link(String link) {
    return link(RuntimeDelegate.getInstance().createHeaderDelegate(Link.class).fromString(link));
  }

  @Override
  public Builder uri(URI uri) {
    uriBuilder = UriBuilder.fromUri(uri);
    return this;
  }

  @Override
  public Builder uri(String uri) {
    uriBuilder = UriBuilder.fromUri(uri);
    return this;
  }

  @Override
  public Builder baseUri(URI uri) {
    this.baseUri = uri;
    return this;
  }

  @Override
  public Builder baseUri(String uri) {
    return baseUri(URI.create(uri));
  }

  @Override
  public Builder uriBuilder(UriBuilder uriBuilder) {
    this.uriBuilder = uriBuilder;
    return this;
  }

  @Override
  public Builder rel(String rel) {
    return param(Link.REL, rel);
  }

  @Override
  public Builder title(String title) {
    return param(Link.TITLE, title);
  }

  @Override
  public Builder type(String type) {
    return param(Link.TYPE, type);
  }

  @Override
  public Builder param(String name, String value) {
    params.put(name, value);
    return this;
  }

  @Override
  public Link build(Object... values) {
    return new LinkImpl(resolveLinkUri(values), params);
  }

  @Override
  public Link buildRelativized(URI uri, Object... values) {
    return new LinkImpl(uri.relativize(resolveLinkUri(values)), params);
  }

  private URI resolveLinkUri(Object[] values) {
    final URI linkUri = uriBuilder.build(values);
    if (baseUri == null || linkUri.isAbsolute()) {
      return linkUri.normalize();
    }
    return baseUri.resolve(linkUri);
  }

}
