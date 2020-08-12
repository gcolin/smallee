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

package net.gcolin.rest.client;

import net.gcolin.common.io.ByteArrayOutputStream;
import net.gcolin.common.io.Io;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.InvocationContext;
import net.gcolin.rest.Logs;
import net.gcolin.rest.MessageBodyWriterDecorator;
import net.gcolin.rest.util.HttpHeader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.WriterInterceptor;

/**
 * The Invocation, AsyncInvoker implementation for REST client.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InvocationImpl implements Invocation, AsyncInvoker {

  private String method;
  private UriBuilder uriBuilder;
  private Map<String, Object> properties;
  private MultivaluedMap<String, Object> headers;
  private ClientFeatureBuilder builder;
  private Entity<?> entity;
  private ClientImpl client;

  /**
   * Create an InvocationImpl.
   * 
   * @param method HTTP method
   * @param uriBuilder UriBuilder
   * @param properties request properties
   * @param headers headers
   * @param builder feature helper
   * @param client client
   * @param entity entity
   */
  public InvocationImpl(String method, UriBuilder uriBuilder, Map<String, Object> properties,
      MultivaluedMap<String, Object> headers, ClientFeatureBuilder builder, ClientImpl client,
      Entity<?> entity) {
    this.builder = builder;
    this.method = method;
    this.properties = new HashMap<>(properties);
    this.uriBuilder = uriBuilder;
    this.headers = new MultivaluedHashMap<>();
    this.headers.putAll(headers);
    this.client = client;
    this.entity = entity;
  }

  @Override
  public Invocation property(String name, Object value) {
    if (value == null) {
      properties.remove(name);
    } else {
      properties.put(name, value);
    }
    return this;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public Response invoke() {
    URI uri = uriBuilder.buildFromMap(properties);

    ClientResponse response = null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    URLConnection conn = null;
    try {

      Map<String, Object> properties = new HashMap<>(this.properties);

      ClientRequestContextImpl ctx = new ClientRequestContextImpl(properties, uri, method, headers,
          client, builder.getConfiguration(), entity, out);

      if (!client.getCookies().isEmpty()) {

        List<NewCookie> list = client.getCookies();
        for (int i = list.size() - 1; i >= 0; i--) {
          NewCookie ncookie = list.get(i);
          if (isExpired(ncookie)) {
            list.remove(i);
          } else if (!ncookie.isSecure() || uri.toString().startsWith("https")) {
            ctx.getHeaders().addAll(HttpHeader.COOKIE,
                new Cookie(ncookie.getName(), ncookie.getValue()));
          }
        }
      }

      if (!builder.getClientRequestFilters().isEmpty()) {
        for (ClientRequestFilter filter : builder.getClientRequestFilters()) {
          filter.filter(ctx);
          if (ctx.getAbortResponse() != null) {
            return ctx.getAbortResponse();
          }
        }
      }

      if (ctx.getEntity() != null) {
        MediaType mediaType = ctx.getMediaType();
        if (mediaType != null) {
          ctx.getHeaders().add(HttpHeader.CONTENT_TYPE, mediaType);
        }

        if (!builder.getWriterInterceptors().isEmpty()) {
          InvocationContext ictx = new InvocationContext(properties);
          ictx.setWriter(builder.getProviders());
          ictx.setProduce(FastMediaType.valueOf(mediaType));
          MessageBodyWriterDecorator md = new MessageBodyWriterDecorator();
          md.add(builder.getWriterInterceptors()
              .toArray(new WriterInterceptor[builder.getWriterInterceptors().size()]));
          md.writeTo(ictx, ctx.getEntity(), ctx.getEntityClass(), ctx.getEntityType(),
              ctx.getEntityAnnotations(), ctx.getHeaders(), out);
        } else {
          builder.getProviders().writeTo(ctx.getEntity(), ctx.getEntityClass(), ctx.getEntityType(),
              ctx.getEntityAnnotations(), mediaType, ctx.getHeaders(), out);
        }
      }

      Logs.LOG_CLIENT.debug("query {} {}", ctx.getMethod(), uri);
      conn = uri.toURL().openConnection();
      boolean hasContent = !out.isEmpty();

      if (conn instanceof HttpsURLConnection) {
        ((HttpsURLConnection) conn).setSSLSocketFactory(client.getSslContext().getSocketFactory());
      }

      if (hasContent) {
        ctx.getHeaders().putSingle(HttpHeader.CONTENT_LENGTH, out.getSize());
        conn.setDoOutput(true);
      }

      addHeaders(conn, ctx.getStringHeaders());
      if (conn instanceof HttpURLConnection) {
        ((HttpURLConnection) conn).setRequestMethod(ctx.getMethod());
      }

      if (hasContent) {
        out.writeTo(conn.getOutputStream());
      }

      conn.connect();
      response = new ClientResponse(conn, builder);
      List<NewCookie> newCookies = (List) response.getHeaders().get(HttpHeader.SET_COOKIE);
      if (newCookies != null) {
        for (NewCookie ncookie : newCookies) {
          String name = ncookie.getName();
          client.getCookies().removeIf(x -> x.getName().equals(name));
          if (ncookie.getMaxAge() > 0) {
            Date date = new Date(System.currentTimeMillis() + (ncookie.getMaxAge() * 1000));
            if (ncookie.getExpiry() == null || ncookie.getExpiry().before(date)) {
              ncookie = new NewCookie(ncookie.getName(), ncookie.getValue(), ncookie.getPath(),
                  ncookie.getDomain(), ncookie.getVersion(), ncookie.getComment(), 0, date,
                  ncookie.isSecure(), ncookie.isHttpOnly());
            }
          }

          if (!isExpired(ncookie)) {
            client.getCookies().add(ncookie);
          }
        }
      }

      if (!builder.getClientResponseFilters().isEmpty()) {
        for (ClientResponseFilter filter : builder.getClientResponseFilters()) {
          filter.filter(ctx, response.getContext());
        }
      }
    } catch (IOException ex) {
      out.release();
      Io.close(conn);
      throw new ResponseProcessingException(response, "cannot send request", ex);
    } finally {
      if (response != null && !response.hasEntity()) {
        response.close();
      }
    }
    return response;
  }

  @Override
  public <T> T invoke(Class<T> responseType) {
    Response response = invoke();
    try {
      return response.readEntity(responseType);
    } finally {
      response.close();
    }
  }

  @Override
  public <T> T invoke(GenericType<T> responseType) {
    Response response = invoke();
    try {
      return response.readEntity(responseType);
    } finally {
      response.close();
    }
  }

  private boolean isExpired(NewCookie ncookie) {
    return ncookie.getExpiry() != null && ncookie.getExpiry().before(new Date());
  }

  private void addHeaders(URLConnection conn, MultivaluedMap<String, String> headers) {
    for (Entry<String, List<String>> header : headers.entrySet()) {
      for (String value : header.getValue()) {
        conn.addRequestProperty(header.getKey(), value);
      }
    }
  }

  @Override
  public Future<Response> submit() {
    return client.getAsyncInvocationExecutor().submit(() -> invoke());
  }

  @Override
  public <T> Future<T> submit(Class<T> responseType) {
    return client.getAsyncInvocationExecutor().submit(() -> invoke(responseType));
  }

  @Override
  public <T> Future<T> submit(GenericType<T> responseType) {
    return client.getAsyncInvocationExecutor().submit(() -> invoke(responseType));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> Future<T> submit(InvocationCallback<T> callback) {
    GenericType<T> genericType = (GenericType<T>) new GenericType<Object>() {};
    List<Type> typeInfo =
        Reflect.getGenericTypeArguments(InvocationCallback.class, callback.getClass(), null);
    if (typeInfo != null && !typeInfo.isEmpty()) {
      genericType = new GenericType(typeInfo.get(0));
    }

    final GenericType<T> responseType = genericType;
    return client.getAsyncInvocationExecutor().submit(new Callable<T>() {
      @Override
      public T call() throws Exception {
        T result = null;
        try {
          result = invoke(responseType);
        } catch (Exception ex) {
          callback.failed(ex);
          throw ex;
        }
        try {
          callback.completed(result);
          return result;
        } finally {
          if (result != null && result instanceof Response) {
            ((Response) result).close();
          }
        }
      }
    });
  }

  @Override
  public Future<Response> get() {
    return method("GET");
  }

  @Override
  public <T> Future<T> get(Class<T> responseType) {
    return method("GET", responseType);
  }

  @Override
  public <T> Future<T> get(GenericType<T> responseType) {
    return method("GET", responseType);
  }

  @Override
  public <T> Future<T> get(InvocationCallback<T> callback) {
    return method("GET", callback);
  }

  @Override
  public Future<Response> put(Entity<?> entity) {
    return method("PUT", entity);
  }

  @Override
  public <T> Future<T> put(Entity<?> entity, Class<T> responseType) {
    return method("PUT", entity, responseType);
  }

  @Override
  public <T> Future<T> put(Entity<?> entity, GenericType<T> responseType) {
    return method("PUT", entity, responseType);
  }

  @Override
  public <T> Future<T> put(Entity<?> entity, InvocationCallback<T> callback) {
    return method("PUT", entity, callback);
  }

  @Override
  public Future<Response> post(Entity<?> entity) {
    return method("POST", entity);
  }

  @Override
  public <T> Future<T> post(Entity<?> entity, Class<T> responseType) {
    return method("POST", entity, responseType);
  }

  @Override
  public <T> Future<T> post(Entity<?> entity, GenericType<T> responseType) {
    return method("POST", entity, responseType);
  }

  @Override
  public <T> Future<T> post(Entity<?> entity, InvocationCallback<T> callback) {
    return method("POST", entity, callback);
  }

  @Override
  public Future<Response> delete() {
    return method("DELETE");
  }

  @Override
  public <T> Future<T> delete(Class<T> responseType) {
    return method("DELETE", responseType);
  }

  @Override
  public <T> Future<T> delete(GenericType<T> responseType) {
    return method("DELETE", responseType);
  }

  @Override
  public <T> Future<T> delete(InvocationCallback<T> callback) {
    return method("DELETE", callback);
  }

  @Override
  public Future<Response> head() {
    return method("HEAD");
  }

  @Override
  public Future<Response> head(InvocationCallback<Response> callback) {
    return method("HEAD", callback);
  }

  @Override
  public Future<Response> options() {
    return method("OPTIONS");
  }

  @Override
  public <T> Future<T> options(Class<T> responseType) {
    return method("OPTIONS", responseType);
  }

  @Override
  public <T> Future<T> options(GenericType<T> responseType) {
    return method("OPTIONS", responseType);
  }

  @Override
  public <T> Future<T> options(InvocationCallback<T> callback) {
    return method("OPTIONS", callback);
  }

  @Override
  public Future<Response> trace() {
    return method("TRACE");
  }

  @Override
  public <T> Future<T> trace(Class<T> responseType) {
    return method("TRACE", responseType);
  }

  @Override
  public <T> Future<T> trace(GenericType<T> responseType) {
    return method("TRACE", responseType);
  }

  @Override
  public <T> Future<T> trace(InvocationCallback<T> callback) {
    return method("TRACE", callback);
  }

  @Override
  public Future<Response> method(String name) {
    method = name;
    return submit();
  }

  @Override
  public <T> Future<T> method(String name, Class<T> responseType) {
    method = name;
    return submit(responseType);
  }

  @Override
  public <T> Future<T> method(String name, GenericType<T> responseType) {
    method = name;
    return submit(responseType);
  }

  @Override
  public <T> Future<T> method(String name, InvocationCallback<T> callback) {
    method = name;
    return submit(callback);
  }

  @Override
  public Future<Response> method(String name, Entity<?> entity) {
    method = name;
    this.entity = entity;
    return submit();
  }

  @Override
  public <T> Future<T> method(String name, Entity<?> entity, Class<T> responseType) {
    method = name;
    this.entity = entity;
    return submit(responseType);
  }

  @Override
  public <T> Future<T> method(String name, Entity<?> entity, GenericType<T> responseType) {
    method = name;
    this.entity = entity;
    return submit(responseType);
  }

  @Override
  public <T> Future<T> method(String name, Entity<?> entity, InvocationCallback<T> callback) {
    method = name;
    this.entity = entity;
    return submit(callback);
  }

}
