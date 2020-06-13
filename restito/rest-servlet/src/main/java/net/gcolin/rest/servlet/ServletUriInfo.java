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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import net.gcolin.common.lang.Strings;
import net.gcolin.rest.Logs;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.util.UrlEncoder;

/**
 * UriInfo from an HttpServletRequest.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServletUriInfo implements UriInfo {

  private static final int HTTP_PORT = 80;
  private MultivaluedMap<String, String> queryParameters;
  private MultivaluedMap<String, String> pathParameters;
  private MultivaluedMap<String, String> queryParametersDecoded;
  private MultivaluedMap<String, String> pathParametersDecoded;
  private HttpServletRequest request;
  private ServerInvocationContext context;

  public ServletUriInfo(HttpServletRequest request, ServerInvocationContext context) {
    this.request = request;
    this.context = context;
  }

  @Override
  public URI getAbsolutePath() {
    try {
      return new URI(request.getRequestURL().toString());
    } catch (URISyntaxException ex) {
      Logs.LOG.error(ex.getMessage(), ex);
      return null;
    }
  }

  @Override
  public UriBuilder getAbsolutePathBuilder() {
    return UriBuilder.fromUri(request.getRequestURL().toString());
  }

  @Override
  public URI getBaseUri() {
    UriBuilder builder = UriBuilder.fromUri(request.getScheme() + "://" + request.getServerName());
    int port = request.getServerPort();
    if (port != HTTP_PORT) {
      builder.port(port);
    }
    if (request.getContextPath().length() > 1) {
      builder.path(request.getContextPath().substring(1) + request.getServletPath());
    } else {
      builder.path(request.getServletPath());
    }
    return builder.build();
  }

  @Override
  public UriBuilder getBaseUriBuilder() {
    return UriBuilder.fromUri(getBaseUri());
  }

  @Override
  public List<Object> getMatchedResources() {
    List<Object> list = new ArrayList<Object>(1);
    list.add(context.getResource().getSource());
    return list;
  }

  @Override
  public List<String> getMatchedURIs() {
    return getMatchedURIs(true);
  }

  @Override
  public List<String> getMatchedURIs(boolean arg0) {
    List<String> list = new ArrayList<String>(1);
    list.add(context.getResource().getPath());
    return list;
  }

  @Override
  public String getPath() {
    return getPath(true);
  }

  @Override
  public String getPath(boolean decode) {
    return decode(request.getRequestURI(), decode);
  }

  @Override
  public MultivaluedMap<String, String> getPathParameters() {
    return getPathParameters(true);
  }

  @Override
  public MultivaluedMap<String, String> getPathParameters(boolean decode) {
    if (decode) {
      if (pathParametersDecoded == null) {
        Map<String, List<String>> params = context.getParams();
        pathParametersDecoded = new MultivaluedHashMap<String, String>();
        for (Entry<String, List<String>> e : params.entrySet()) {
          for (String item : e.getValue()) {
            pathParametersDecoded.add(e.getKey(), decode(item, true));
          }
        }
      }
      return pathParametersDecoded;
    } else {
      if (pathParameters == null) {
        Map<String, List<String>> params = context.getParams();
        pathParameters = new MultivaluedHashMap<String, String>();
        for (Entry<String, List<String>> e : params.entrySet()) {
          pathParameters.put(e.getKey(), e.getValue());
        }
      }
      return pathParameters;
    }
  }

  @Override
  public List<PathSegment> getPathSegments() {
    return getPathSegments(true);
  }

  @Override
  public List<PathSegment> getPathSegments(boolean decode) {
    throw new UnsupportedOperationException();
  }

  private String decode(String str, boolean decode) {
    if (str == null) {
      return null;
    }
    if (decode) {
      return Strings.decodeUrl(str);
    }
    return str;
  }

  @Override
  public MultivaluedMap<String, String> getQueryParameters() {
    return getQueryParameters(true);
  }

  @Override
  public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
    if (decode) {
      if (queryParametersDecoded == null) {
        queryParametersDecoded = new MultivaluedHashMap<String, String>();
        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
          for(String value : entry.getValue()) {
            queryParametersDecoded.add(entry.getKey(), value);
          }
        }
      }
      return queryParametersDecoded;
    } else {
      if (queryParameters == null) {
        queryParameters = new MultivaluedHashMap<String, String>();
        for (Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
          for(String value : entry.getValue()) {
            queryParameters.add(entry.getKey(), UrlEncoder.DEFAULT.encode(value));
          }
        }
      }
      return queryParameters;
    }
  }

  @Override
  public URI getRequestUri() {
    try {
      return new URI(request.getRequestURI());
    } catch (URISyntaxException ex) {
      Logs.LOG.error(ex.getMessage(), ex);
      return null;
    }
  }

  @Override
  public UriBuilder getRequestUriBuilder() {
    return UriBuilder.fromUri(getRequestUri());
  }

  @Override
  public URI resolve(URI uri) {
    throw new UnsupportedOperationException();
  }

  @Override
  public URI relativize(URI uri) {
    throw new UnsupportedOperationException();
  }

}
