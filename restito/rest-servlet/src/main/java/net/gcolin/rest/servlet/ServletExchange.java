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

import net.gcolin.rest.server.Exchange;
import net.gcolin.rest.server.ServerInvocationContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * Implementation of Exchange.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServletExchange implements Exchange {

  private HttpServletRequest request;
  private HttpServletResponse response;
  private RestServlet servlet;
  private HttpResponseObserver responseObserver;
  private OutputStream output;

  /**
   * Create a ServletExchange.
   * 
   * @param request servlet request
   * @param response servlet response
   * @param servlet calling servlet
   */
  public ServletExchange(HttpServletRequest request, HttpServletResponse response,
      RestServlet servlet) {
    super();
    this.request = request;
    this.response = response;
    this.servlet = servlet;
  }

  /**
   * Get the HttpResponseObserver.
   * 
   * @return the HttpResponseObserver
   */
  public HttpResponseObserver getResponseObserver() {
    if (responseObserver == null) {
      responseObserver = new HttpResponseObserver(response);
    }
    return responseObserver;
  }

  public boolean hasWritten() {
    return responseObserver != null && responseObserver.isHasWritten();
  }

  public RestServlet getServlet() {
    return servlet;
  }

  public ServletConfig getConfig() {
    return servlet.getServletConfig();
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  @Override
  public Request request() {
    return new ServletRequest(request);
  }

  @Override
  public String method() {
    return request.getMethod();
  }

  @Override
  public HttpHeaders headers() {
    return new ServletHttpHeaders(request);
  }

  @Override
  public Locale locale() {
    Locale locale = null;
    if (request != null) {
      HttpSession session = request.getSession(false);
      if (session != null) {
        locale = (Locale) session.getAttribute("javax.servlet.jsp.jstl.fmt.locale.session");
        if (locale != null) {
          return locale;
        }
      }
      locale = request.getLocale();
    }
    if (locale == null) {
      locale = Locale.getDefault();
    }
    return locale;
  }

  @Override
  public int length() {
    return request.getContentLength();
  }

  @Override
  public InputStream inputStream() {
    try {
      return request.getInputStream();
    } catch (IOException ex) {
      throw new InternalServerErrorException(ex);
    }
  }

  @Override
  public OutputStream outputStream() {
    if (output == null) {
      output = new LazyOutputStream(() -> response.getOutputStream());
    }
    return output;
  }

  @Override
  public int outlength() {
    return response.getBufferSize();
  }

  @Override
  public SecurityContext securityContext() {
    return new SecurityContextWrapper(request);
  }

  @Override
  public UriInfo uriInfo(ServerInvocationContext ctx) {
    return new ServletUriInfo(request, ctx);
  }

}
