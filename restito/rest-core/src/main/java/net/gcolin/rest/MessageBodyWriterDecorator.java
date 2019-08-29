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

import net.gcolin.rest.util.Filters;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * WriterInterceptor container.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MessageBodyWriterDecorator {

  private List<WriterInterceptor> interceptors = new ArrayList<>();

  /**
   * Add interceptors.
   * 
   * @param interceptorArray interceptors
   */
  public void add(WriterInterceptor... interceptorArray) {
    for (int i = 0; i < interceptorArray.length; i++) {
      interceptors.add(interceptorArray[i]);
    }
    Collections.sort(interceptors, Filters.SORT);
  }

  public void writeTo(InvocationContext context, Object entity, Class<?> type, Type genericType,
      Annotation[] annotations, MultivaluedMap<String, Object> httpHeaders,
      OutputStream entityStream) throws IOException {
    new WriterInterceptorContextImpl(context, type, genericType, annotations, entityStream, entity,
        httpHeaders).proceed();
  }

  private class WriterInterceptorContextImpl extends InterceptorContextImpl
      implements
        WriterInterceptorContext {

    private OutputStream aentityStream;
    private Object entity;
    private MultivaluedMap<String, Object> httpHeaders;
    private int index = 0;
    private InvocationContext context;

    public WriterInterceptorContextImpl(InvocationContext context, Class<?> atype,
        Type agenericType, Annotation[] aannotations, OutputStream aentityStream, Object entity,
        MultivaluedMap<String, Object> httpHeaders) {
      super(context, aannotations, atype, agenericType);
      this.aentityStream = aentityStream;
      this.entity = entity;
      this.httpHeaders = httpHeaders;
      this.context = context;
    }

    @Override
    public MediaType getMediaType() {
      return context.getProduce();
    }

    @Override
    public void setMediaType(MediaType mediaType) {
      context.setProduce(FastMediaType.valueOf(mediaType));
    }

    @Override
    public void setOutputStream(OutputStream os) {
      aentityStream = os;
    }

    @Override
    public void setEntity(Object entity) {
      this.entity = entity;
    }

    @Override
    public void proceed() throws IOException {
      if (index == interceptors.size()) {
        context.getWriter().writeTo(entity, getType(), getGenericType(), getAnnotations(),
            context.getProduce(), httpHeaders, aentityStream);
      } else {
        interceptors.get(index++).aroundWriteTo(this);
      }
    }

    @Override
    public OutputStream getOutputStream() {
      return aentityStream;
    }

    @Override
    public MultivaluedMap<String, Object> getHeaders() {
      return httpHeaders;
    }

    @Override
    public Object getEntity() {
      return entity;
    }
  }
}
