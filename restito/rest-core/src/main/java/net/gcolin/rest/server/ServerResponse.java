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

import net.gcolin.common.collection.Collections2;
import net.gcolin.common.io.Io;
import net.gcolin.rest.AbstractResponse;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

/**
 * The Response implementation of the REST server side.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServerResponse extends AbstractResponse {

  private Object entity;
  private Type genericType;
  private MultivaluedMap<String, Object> headers;
  private MultivaluedMap<String, String> stringheaders;
  private int status;
  private boolean buffered;
  private Annotation[] annotations;
  private Annotation[] entityAnnotations;
  private OutputStream outputStream;

  /**
   * Create a server response.
   * 
   * @param entity an entity
   * @param headers the response header
   * @param stringheaders the response header in text format
   * @param outputStream the server output stream
   * @param status the response status
   * @param annotations the annotations of the rest service
   * @param entityAnnotations the annotations of the entity
   */
  public ServerResponse(Object entity, MultivaluedMap<String, Object> headers,
      MultivaluedMap<String, String> stringheaders, OutputStream outputStream, int status,
      Annotation[] annotations, Annotation[] entityAnnotations) {
    this.entity = entity;
    this.headers = headers;
    this.stringheaders = stringheaders;
    this.status = status;
    this.outputStream = outputStream;
    this.annotations = annotations;
    this.entityAnnotations = entityAnnotations;
  }

  @Override
  public MultivaluedMap<String, Object> getMetadata() {
    return headers;
  }

  public Annotation[] getAllAnnotations() {
    return Collections2.merge(annotations, entityAnnotations);
  }

  @Override
  public Object getEntity() {
    return entity;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasEntity() {
    return entity != null;
  }

  @Override
  public boolean bufferEntity() {
    return false;
  }

  @Override
  public void close() {
    if (buffered) {
      // release buffer
      entity = null;
    } else if (hasEntity() && InputStream.class.isAssignableFrom(entity.getClass())) {
      Io.close(InputStream.class.cast(entity));
    }
  }

  @Override
  public MultivaluedMap<String, String> getStringHeaders() {
    return stringheaders;
  }

  private class ContainerContext implements ContainerResponseContext {

    @Override
    public int getStatus() {
      return status;
    }

    @Override
    public void setStatus(int code) {
      status = code;
    }

    @Override
    public StatusType getStatusInfo() {
      return Status.fromStatusCode(getStatus());
    }

    @Override
    public void setStatusInfo(StatusType statusInfo) {
      setStatus(statusInfo.getStatusCode());
    }

    @Override
    public MultivaluedMap<String, Object> getHeaders() {
      return headers;
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
      return ServerResponse.this.getStringHeaders();
    }

    @Override
    public String getHeaderString(String name) {
      return ServerResponse.this.getHeaderString(name);
    }

    @Override
    public Set<String> getAllowedMethods() {
      return ServerResponse.this.getAllowedMethods();
    }

    @Override
    public Date getDate() {
      return ServerResponse.this.getDate();
    }

    @Override
    public Locale getLanguage() {
      return ServerResponse.this.getLanguage();
    }

    @Override
    public int getLength() {
      return ServerResponse.this.getLength();
    }

    @Override
    public MediaType getMediaType() {
      return ServerResponse.this.getMediaType();
    }

    @Override
    public Map<String, NewCookie> getCookies() {
      return ServerResponse.this.getCookies();
    }

    @Override
    public EntityTag getEntityTag() {
      return ServerResponse.this.getEntityTag();
    }

    @Override
    public Date getLastModified() {
      return ServerResponse.this.getLastModified();
    }

    @Override
    public URI getLocation() {
      return ServerResponse.this.getLocation();
    }

    @Override
    public Set<Link> getLinks() {
      return ServerResponse.this.getLinks();
    }

    @Override
    public boolean hasLink(String relation) {
      return ServerResponse.this.hasLink(relation);
    }

    @Override
    public Link getLink(String relation) {
      return ServerResponse.this.getLink(relation);
    }

    @Override
    public Builder getLinkBuilder(String relation) {
      return ServerResponse.this.getLinkBuilder(relation);
    }

    @Override
    public boolean hasEntity() {
      return ServerResponse.this.hasEntity();
    }

    @Override
    public Object getEntity() {
      return entity;
    }

    @Override
    public Class<?> getEntityClass() {
      return entity != null ? entity.getClass() : null;
    }

    @Override
    public Type getEntityType() {
      return genericType;
    }

    @Override
    public void setEntity(Object newEntity) {
      entity = newEntity;
      if (entity != null) {
        if (entity instanceof GenericEntity) {
          GenericEntity<?> ge = (GenericEntity<?>) entity;
          genericType = ge.getRawType();
        } else {
          genericType = entity.getClass();
        }
      }
    }

    @Override
    public void setEntity(Object entity, Annotation[] annotation, MediaType mediaType) {
      setEntity(entity);
      if (annotation != null) {
        entityAnnotations = annotation;
      }
      getHeaders().add(HttpHeaders.CONTENT_TYPE, mediaType);
    }

    @Override
    public Annotation[] getEntityAnnotations() {
      return entityAnnotations;
    }

    @Override
    public OutputStream getEntityStream() {
      return outputStream;
    }

    @Override
    public void setEntityStream(OutputStream outputStream) {
      ServerResponse.this.outputStream = outputStream;
    }

  }

  public ContainerResponseContext newContext() {
    return new ContainerContext();
  }

}
