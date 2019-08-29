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
package net.gcolin.di.atinject.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.gcolin.di.atinject.Environment;

/**
 * Filter for injecting the ServletResponse.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DiFilter implements Filter {

  public static final String DI_INSTANCES = "diall";
  public static final String DIENV = "dienv";
  
  private final ThreadLocal<ServletResponse> response =
      new ThreadLocal<>();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Environment env =  (Environment) filterConfig.getServletContext().getAttribute(DiFilter.DIENV);
    env.getExtension(WebExtension.class).setResponseProvider(() -> response.get());
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain)
      throws IOException, ServletException {
    try {
      response.set(resp);
      chain.doFilter(request, resp);
    } finally {
      response.remove();
    }
  }

  @Override
  public void destroy() {}

}
