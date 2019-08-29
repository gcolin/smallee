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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

/**
 * ReaderInterceptor container.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MessageBodyReaderDecorator {

  private List<ReaderInterceptor> interceptors = new ArrayList<>();

  /**
   * Add interceptors.
   * 
   * @param interceptorArray interceptors
   */
  public void add(ReaderInterceptor... interceptorArray) {
    for (int i = 0; i < interceptorArray.length; i++) {
      interceptors.add(interceptorArray[i]);
    }
    Collections.sort(interceptors, Filters.SORT);
  }

  public Object readFrom(InvocationContext context, Annotation[] annotations, Class<?> type,
      Type genericType, MultivaluedMap<String, String> headers) throws IOException {
    return new ReaderInterceptorContextImpl(context, annotations, type, genericType, headers, 0)
        .proceed();
  }

  class ReaderInterceptorContextImpl extends InterceptorContextImpl
      implements
        ReaderInterceptorContext {

    private MultivaluedMap<String, String> headers;
    private int index;
    private InvocationContext context;

    public ReaderInterceptorContextImpl(InvocationContext context, Annotation[] annotations,
        Class<?> type, Type genericType, MultivaluedMap<String, String> headers, int index) {
      super(context, annotations, type, genericType);
      this.headers = headers;
      this.index = index;
      this.context = context;
    }

    @Override
    public MediaType getMediaType() {
      return context.getConsume();
    }

    @Override
    public void setMediaType(MediaType mediaType) {
      context.setConsume(FastMediaType.valueOf(mediaType));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object proceed() throws IOException {
      if (index == interceptors.size()) {
        return context.getReader().readFrom((Class<Object>) getType(), getGenericType(),
            getAnnotations(), context.getConsume(), headers, context.getEntityStream());
      } else {
        return interceptors.get(index++).aroundReadFrom(this);
      }
    }

    @Override
    public InputStream getInputStream() {
      return context.getEntityStream();
    }

    @Override
    public void setInputStream(InputStream is) {
      context.setEntityStream(is);
    }

    @Override
    public MultivaluedMap<String, String> getHeaders() {
      return headers;
    }

  }
}
