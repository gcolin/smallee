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

import net.gcolin.common.lang.Locales;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.util.HttpHeader;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * The Invocation.Builder implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class BuilderImpl implements Builder {

  private static final String TRACE = "TRACE";
  private static final String OPTIONS = "OPTIONS";
  private UriBuilder uriBuilder;
  private ClientImpl clientImpl;
  private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
  private ClientFeatureBuilder builder;
  private Map<String, Object> properties;

  BuilderImpl(UriBuilder uriBuilder, ClientImpl clientImpl, Map<String, Object> properties,
      ClientFeatureBuilder builder) {
    this.uriBuilder = uriBuilder;
    this.properties = new HashMap<>(properties);
    this.clientImpl = clientImpl;
    this.builder = builder;
  }

  @Override
  public Response get() {
    return buildGet().invoke();
  }

  @Override
  public <T> T get(Class<T> responseType) {
    return buildGet().invoke(responseType);
  }

  @Override
  public <T> T get(GenericType<T> responseType) {
    return buildGet().invoke(responseType);
  }

  @Override
  public Response put(Entity<?> entity) {
    return buildPut(entity).invoke();
  }

  @Override
  public <T> T put(Entity<?> entity, Class<T> responseType) {
    return buildPut(entity).invoke(responseType);
  }

  @Override
  public <T> T put(Entity<?> entity, GenericType<T> responseType) {
    return buildPut(entity).invoke(responseType);
  }

  @Override
  public Response post(Entity<?> entity) {
    return buildPost(entity).invoke();
  }

  @Override
  public <T> T post(Entity<?> entity, Class<T> responseType) {
    return buildPost(entity).invoke(responseType);
  }

  @Override
  public <T> T post(Entity<?> entity, GenericType<T> responseType) {
    return buildPost(entity).invoke(responseType);
  }

  @Override
  public Response delete() {
    return buildDelete().invoke();
  }

  @Override
  public <T> T delete(Class<T> responseType) {
    return buildDelete().invoke(responseType);
  }

  @Override
  public <T> T delete(GenericType<T> responseType) {
    return buildDelete().invoke(responseType);
  }

  @Override
  public Response head() {
    return build("HEAD").invoke();
  }

  @Override
  public Response options() {
    return build(OPTIONS).invoke();
  }

  @Override
  public <T> T options(Class<T> responseType) {
    return build(OPTIONS).invoke(responseType);
  }

  @Override
  public <T> T options(GenericType<T> responseType) {
    return build(OPTIONS).invoke(responseType);
  }

  @Override
  public Response trace() {
    return build(TRACE).invoke();
  }

  @Override
  public <T> T trace(Class<T> responseType) {
    return build(TRACE).invoke(responseType);
  }

  @Override
  public <T> T trace(GenericType<T> responseType) {
    return build(TRACE).invoke(responseType);
  }

  @Override
  public Response method(String name) {
    return build(name).invoke();
  }

  @Override
  public <T> T method(String name, Class<T> responseType) {
    return build(name).invoke(responseType);
  }

  @Override
  public <T> T method(String name, GenericType<T> responseType) {
    return build(name).invoke(responseType);
  }

  @Override
  public Response method(String name, Entity<?> entity) {
    return build(name, entity).invoke();
  }

  @Override
  public <T> T method(String name, Entity<?> entity, Class<T> responseType) {
    return build(name, entity).invoke(responseType);
  }

  @Override
  public <T> T method(String name, Entity<?> entity, GenericType<T> responseType) {
    return build(name, entity).invoke(responseType);
  }

  @Override
  public Invocation build(String method) {
    clientImpl.checkOpen();
    return new InvocationImpl(method, uriBuilder, properties, headers, builder, clientImpl, null);
  }

  @Override
  public Invocation build(String method, Entity<?> entity) {
    clientImpl.checkOpen();
    return new InvocationImpl(method, uriBuilder, properties, headers, builder, clientImpl, entity);
  }

  @Override
  public Invocation buildGet() {
    return build("GET");
  }

  @Override
  public Invocation buildDelete() {
    return build("DELETE");
  }

  @Override
  public Invocation buildPost(Entity<?> entity) {
    return build("POST", entity);
  }

  @Override
  public Invocation buildPut(Entity<?> entity) {
    return build("PUT", entity);
  }

  @Override
  public AsyncInvoker async() {
    clientImpl.checkOpen();
    return new InvocationImpl(null, uriBuilder, properties, headers, builder, clientImpl, null);
  }

  @Override
  public Builder accept(String... mediaTypes) {
    for (String l : mediaTypes) {
      headers.add(HttpHeader.ACCEPT, FastMediaType.valueOf(l));
    }
    return this;
  }

  @Override
  public Builder accept(MediaType... mediaTypes) {
    for (MediaType l : mediaTypes) {
      headers.add(HttpHeader.ACCEPT, l);
    }
    return this;
  }

  @Override
  public Builder acceptLanguage(Locale... locales) {
    for (Locale l : locales) {
      headers.add(HttpHeader.ACCEPT_LANGUAGE, l);
    }
    return this;
  }

  @Override
  public Builder acceptLanguage(String... locales) {
    for (String l : locales) {
      headers.add(HttpHeader.ACCEPT_LANGUAGE, Locales.fromString(l));
    }
    return this;
  }

  @Override
  public Builder acceptEncoding(String... encodings) {
    for (String l : encodings) {
      headers.add(HttpHeader.ACCEPT_ENCODING, l);
    }
    return this;
  }

  @Override
  public Builder cookie(Cookie cookie) {
    return header0(HttpHeader.SET_COOKIE, cookie);
  }

  @Override
  public Builder cookie(String name, String value) {
    return cookie(new Cookie(name, value));
  }

  @Override
  public Builder cacheControl(CacheControl cacheControl) {
    return header0(HttpHeader.CACHE_CONTROL, cacheControl);
  }

  @Override
  public Builder header(String name, Object value) {
    return header0(name.toLowerCase(), value);
  }

  private Builder header0(String name, Object value) {
    headers.add(name, value);
    return this;
  }

  @Override
  public Builder headers(MultivaluedMap<String, Object> headers) {
    this.headers = new MultivaluedHashMap<>();
    for (Entry<String, List<Object>> e : headers.entrySet()) {
      this.headers.put(e.getKey().toLowerCase(), e.getValue());
    }
    return this;
  }

  @Override
  public Builder property(String name, Object value) {
    if (value == null) {
      properties.remove(name);
    } else {
      properties.put(name, value);
    }
    return this;
  }

}
