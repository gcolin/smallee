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

import net.gcolin.rest.util.HeaderObjectMap;
import net.gcolin.rest.util.HeaderPair;
import net.gcolin.rest.util.HttpHeader;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * The ClientRequestContext implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ClientRequestContextImpl implements ClientRequestContext {

  private Map<String, Object> properties;
  private URI uri;
  private String method;
  private MultivaluedMap<String, Object> headers;
  private MultivaluedMap<String, String> stringheaders;
  private Client client;
  private Response abortResponse;
  private Entity<?> entity;
  private Configuration config;
  private OutputStream out;

  ClientRequestContextImpl(Map<String, Object> properties, URI uri, String method,
      MultivaluedMap<String, Object> headers, Client client, Configuration config, Entity<?> entity,
      OutputStream out) {
    this.properties = properties;
    this.uri = uri;
    this.method = method;
    HeaderPair pair = HeaderObjectMap.createHeaders();
    this.headers = pair.getKey();
    this.headers.putAll(headers);
    this.stringheaders = pair.getValue();
    this.client = client;
    this.config = config;
    this.entity = entity;
    this.out = out;
  }

  public Response getAbortResponse() {
    return abortResponse;
  }

  @Override
  public Object getProperty(String name) {
    return properties.get(name);
  }

  @Override
  public Collection<String> getPropertyNames() {
    return properties.keySet();
  }

  @Override
  public void setProperty(String name, Object object) {
    properties.put(name, object);
  }

  @Override
  public void removeProperty(String name) {
    properties.remove(name);
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public void setUri(URI uri) {
    this.uri = uri;
  }

  @Override
  public String getMethod() {
    return method;
  }

  @Override
  public void setMethod(String method) {
    this.method = method;
  }

  @Override
  public MultivaluedMap<String, Object> getHeaders() {
    return headers;
  }

  @Override
  public MultivaluedMap<String, String> getStringHeaders() {
    return stringheaders;
  }

  @Override
  public String getHeaderString(String name) {
    return stringheaders.getFirst(name);
  }

  @Override
  public Date getDate() {
    return (Date) getHeaders().getFirst(HttpHeaders.DATE);
  }

  @Override
  public Locale getLanguage() {
    return entity == null ? null : entity.getLanguage();
  }

  @Override
  public MediaType getMediaType() {
    return entity == null ? null : entity.getMediaType();
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public List<MediaType> getAcceptableMediaTypes() {
    return (List) getHeaders().get(HttpHeader.ACCEPT);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public List<Locale> getAcceptableLanguages() {
    return (List) getHeaders().get(HttpHeader.ACCEPT_LANGUAGE);
  }

  @Override
  public Map<String, Cookie> getCookies() {
    @SuppressWarnings({"unchecked", "rawtypes"})
    List<Cookie> list = (List) getHeaders().get(HttpHeader.COOKIE);
    if (list.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<String, Cookie> map = new HashMap<>();
    for (int i = 0; i < list.size(); i++) {
      map.put(list.get(i).getName(), list.get(i));
    }
    return map;
  }

  @Override
  public boolean hasEntity() {
    return entity != null;
  }

  @Override
  public Object getEntity() {
    return entity == null ? null : entity.getEntity();
  }

  @Override
  public Class<?> getEntityClass() {
    if (entity == null) {
      return null;
    }
    Object obj = entity.getEntity();
    return obj instanceof GenericEntity ? ((GenericEntity<?>) obj).getRawType() : obj.getClass();
  }

  @Override
  public Type getEntityType() {
    if (entity == null) {
      return null;
    }
    Object obj = entity.getEntity();
    return obj instanceof GenericEntity ? ((GenericEntity<?>) obj).getType() : obj.getClass();
  }

  @Override
  public void setEntity(Object entity) {
    if (entity instanceof Entity) {
      this.entity = (Entity<?>) entity;
    } else {
      this.entity = Entity.entity(entity, getHeaderString(HttpHeaders.CONTENT_TYPE));
    }
  }

  @Override
  public void setEntity(Object entity, Annotation[] annotations, MediaType mediaType) {
    this.entity = Entity.entity(entity, mediaType, annotations);
  }

  @Override
  public Annotation[] getEntityAnnotations() {
    return entity == null ? null : entity.getAnnotations();
  }

  @Override
  public OutputStream getEntityStream() {
    return out;
  }

  @Override
  public void setEntityStream(OutputStream outputStream) {
    this.out = outputStream;
  }

  @Override
  public Client getClient() {
    return client;
  }

  @Override
  public Configuration getConfiguration() {
    return config;
  }

  @Override
  public void abortWith(Response response) {
    this.abortResponse = response;
  }

}
