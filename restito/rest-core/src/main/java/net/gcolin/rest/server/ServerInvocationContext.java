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

package net.gcolin.rest.server;

import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.InvocationContext;
import net.gcolin.rest.util.LazyMultivaluedMap;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * The InvocationContext implementation of the REST server side.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServerInvocationContext extends InvocationContext implements ContainerRequestContext {

  private AbstractResource resource;
  private Map<String, List<String>> params;
  private UriInfo uriInfo;
  private Request request;
  private HttpHeaders headers;
  private MultivaluedMap<String, String> lazyHeaders;
  private String method;
  private SecurityContext securityContext;
  private Response abortResponse;
  private InputStream inputStream;
  private OutputStream outputStream;
  private Exchange exchange;
  private int status = HttpURLConnection.HTTP_OK;
  private Map<String, NewCookie> cookies;
  private Class<?> entityClass;
  private Type entityGenericType;
  private URI newUri;

  public ServerInvocationContext(Exchange endpoint) {
    this.exchange = endpoint;
  }

  public Exchange getExchange() {
    return exchange;
  }

  public AbstractResource getResource() {
    return resource;
  }

  public ServerInvocationContext setResource(AbstractResource resource) {
    this.resource = resource;
    return this;
  }

  /**
   * Get the new cookies.
   * 
   * @return the new cookies
   */
  public Map<String, NewCookie> getNewCookies() {
    if (cookies == null) {
      cookies = new HashMap<>();
    }
    return cookies;
  }

  public boolean hasNewCookies() {
    return cookies != null && !cookies.isEmpty();
  }

  public Map<String, List<String>> getParams() {
    return params;
  }

  public Class<?> getEntityClass() {
    return entityClass;
  }

  public void setEntityClass(Class<?> entityClass) {
    this.entityClass = entityClass;
  }

  public Type getEntityGenericType() {
    return entityGenericType;
  }

  public void setEntityGenericType(Type entityGenericType) {
    this.entityGenericType = entityGenericType;
  }

  public void setParams(Map<String, List<String>> params) {
    this.params = params;
  }

  @Override
  public UriInfo getUriInfo() {
    if (uriInfo == null) {
      uriInfo = exchange.uriInfo(this);
    }
    return uriInfo;
  }

  @Override
  public void setRequestUri(URI requestUri) {
    setRequestUri(getUriInfo().getRequestUri(), requestUri);
  }

  @Override
  public void setRequestUri(URI baseUri, URI requestUri) {
    newUri = requestUri.resolve(baseUri);
  }

  public URI getNewUri() {
    return newUri;
  }

  @Override
  public Request getRequest() {
    if (request == null) {
      request = exchange.request();
    }
    return request;
  }

  @Override
  public String getMethod() {
    if (method == null) {
      method = exchange.method();
    }
    return method;
  }

  @Override
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Get the HTTP headers.
   * 
   * @return the HTTP headers
   */
  public HttpHeaders getHttpHeaders() {
    if (headers == null) {
      headers = exchange.headers();
    }
    return headers;
  }

  @Override
  public MultivaluedMap<String, String> getHeaders() {
    if (lazyHeaders == null) {
      lazyHeaders =
          new LazyMultivaluedMap<String, String>(() -> getHttpHeaders().getRequestHeaders());
    }
    return lazyHeaders;
  }

  @Override
  public String getHeaderString(String name) {
    return getHttpHeaders().getHeaderString(name);
  }

  @Override
  public Date getDate() {
    String hdate = getHeaderString(HttpHeaders.DATE);
    if (hdate == null) {
      return null;
    }
    return GregorianCalendar
        .from(ZonedDateTime.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(hdate))).getTime();
  }

  @Override
  public Locale getLanguage() {
    return exchange.locale();
  }

  @Override
  public int getLength() {
    return exchange.length();
  }

  @Override
  public MediaType getMediaType() {
    if (getConsume() != null) {
      return getConsume();
    }
    String hmtype = getHeaderString(HttpHeaders.CONTENT_TYPE);
    return hmtype == null ? null : FastMediaType.valueOf(hmtype);
  }

  @Override
  public List<MediaType> getAcceptableMediaTypes() {
    return getHttpHeaders().getAcceptableMediaTypes();
  }

  @Override
  public List<Locale> getAcceptableLanguages() {
    return getHttpHeaders().getAcceptableLanguages();
  }

  @Override
  public Map<String, Cookie> getCookies() {
    return getHttpHeaders().getCookies();
  }

  @Override
  public boolean hasEntity() {
    return getEntityStream() != null;
  }

  @Override
  public InputStream getEntityStream() {
    if (inputStream == null) {
      inputStream = exchange.inputStream();
    }
    return inputStream;
  }

  /**
   * Get the OutputStream.
   * 
   * @return the OutputStream
   */
  public OutputStream getOutputStream() {
    if (outputStream == null) {
      outputStream = exchange.outputStream();
    }
    return outputStream;
  }

  public void setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public void setEntityStream(InputStream input) {
    this.inputStream = input;
  }

  @Override
  public SecurityContext getSecurityContext() {
    if (securityContext == null) {
      securityContext = exchange.securityContext();
    }
    return securityContext;
  }

  @Override
  public void setSecurityContext(SecurityContext context) {
    securityContext = context;
  }

  @Override
  public void abortWith(Response response) {
    abortResponse = response;
  }

  public Response getAbortResponse() {
    return abortResponse;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }


}
