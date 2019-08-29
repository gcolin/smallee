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

import net.gcolin.rest.provider.StringProviderExtension;
import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.servlet.ServletExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Write jsp in a transparent way.
 * 
 * <p>When string ends by ".jsp" this provider will render the associated jsp.
 * It is similar to CXF but it does not require any configuration.</p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JspStringProviderExtension implements StringProviderExtension {

  @Override
  public boolean writeTo(String entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
          throws IOException {
    if (!entity.endsWith(".jsp")) {
      return false;
    }
    ServletExchange exchange = (ServletExchange) Contexts.instance().get().getExchange();
    HttpServletRequest request = exchange.getRequest();
    HttpServletResponse response = exchange.getResponse();

    try {
      request.getServletContext().getRequestDispatcher(entity).forward(request, response);
    } catch (ServletException ex) {
      throw new IOException(ex);
    }
    return true;
  }

}
