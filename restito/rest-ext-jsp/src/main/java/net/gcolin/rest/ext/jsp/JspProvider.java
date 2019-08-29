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

package net.gcolin.rest.ext.jsp;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import net.gcolin.common.Priority;

/**
 * Write JspView.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@ConstrainedTo(RuntimeType.SERVER)
@Produces({MediaType.WILDCARD})
@Priority(10)
public class JspProvider implements MessageBodyWriter<JspView> {

  @Context
  HttpServletResponse response;
  @Context
  HttpServletRequest request;

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type == JspView.class;
  }

  @Override
  public long getSize(JspView entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(JspView entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
      final OutputStream entityStream) throws IOException {
    if (entity.getAttributes() != null) {
      for (Entry<String, Object> e : entity.getAttributes().entrySet()) {
        request.setAttribute(e.getKey(), e.getValue());
      }
    }

    try {
      request.getServletContext().getRequestDispatcher(entity.getPath()).forward(request, response);
    } catch (ServletException ex) {
      throw new WebApplicationException(ex);
    }
  }

}
