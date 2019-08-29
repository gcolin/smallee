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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * A Link implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LinkImpl extends Link {

  private URI uri;
  private Map<String, String> params;

  public LinkImpl(URI uri, Map<String, String> params) {
    this.uri = uri;
    this.params = Collections.unmodifiableMap(params);
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public UriBuilder getUriBuilder() {
    return RuntimeDelegate.getInstance().createUriBuilder().uri(uri);
  }

  @Override
  public String getRel() {
    return params.get(REL);
  }

  @Override
  public List<String> getRels() {
    String rel = getRel();
    return rel == null ? Collections.emptyList() : Arrays.asList(rel);
  }

  @Override
  public String getTitle() {
    return params.get(TITLE);
  }

  @Override
  public String getType() {
    return params.get(TYPE);
  }

  @Override
  public Map<String, String> getParams() {
    return params;
  }

  @Override
  public String toString() {
    return RuntimeDelegate.getInstance().createHeaderDelegate(Link.class).toString(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + params.hashCode();
    result = prime * result + uri.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    LinkImpl other = (LinkImpl) obj;
    return uri.equals(other.uri) && params.equals(other.params);
  }
}
