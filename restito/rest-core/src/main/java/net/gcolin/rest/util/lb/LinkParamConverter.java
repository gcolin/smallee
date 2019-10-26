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

import java.net.URI;
import java.util.Map.Entry;

import javax.ws.rs.core.Link;

import net.gcolin.rest.LinkImpl;
import net.gcolin.rest.util.Headers;

/**
 * Converter String to Link.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LinkParamConverter implements Converter<Link> {

  @Override
  public Link fromString(String value) {
    int startUri = value.indexOf('<');
    int endUri = value.indexOf('>');
    if (startUri == -1 || endUri == -1) {
      throw new IllegalArgumentException("bad link header");
    }
    URI uri = URI.create(value.substring(startUri + 1, endUri));
    return new LinkImpl(uri, Headers.getParameters(value.substring(endUri + 1).toLowerCase()));
  }

  @Override
  public String toString(Link value) {
    StringBuilder sb = new StringBuilder();
    sb.append('<').append(value.getUri().toString()).append("> ");
    for (Entry<String, String> param : value.getParams().entrySet()) {
      sb.append("; ").append(param.getKey()).append("=\"").append(param.getValue()).append("\"");
    }
    return sb.toString();
  }

}
