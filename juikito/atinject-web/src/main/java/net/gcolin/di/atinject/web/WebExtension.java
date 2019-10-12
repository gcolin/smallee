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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.atinject.SupplierProvider;

/**
 * Enable the web scopes.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class WebExtension implements Extension {

  private Set<Class<? extends Annotation>> requestAnnotations = new HashSet<>();
  private Set<Class<? extends Annotation>> sessionAnnotations = new HashSet<>();
  private Set<Class<? extends Annotation>> applicationAnnotations = new HashSet<>();
  private Supplier<ServletRequest> requestProvider = () -> null;
  private Supplier<ServletResponse> responseProvider = () -> null;
  private Supplier<ServletContext> contextProvider = () -> null;

  public WebExtension() {
    requestAnnotations.add(RequestScoped.class);
    sessionAnnotations.add(SessionScoped.class);
    applicationAnnotations.add(ApplicationScoped.class);
  }
  
  @Override
  public int priority() {
    return 1000;
  }

  public Set<Class<? extends Annotation>> getRequestAnnotations() {
    return requestAnnotations;
  }

  public Set<Class<? extends Annotation>> getSessionAnnotations() {
    return sessionAnnotations;
  }

  public Set<Class<? extends Annotation>> getApplicationAnnotations() {
    return applicationAnnotations;
  }
  
  public Supplier<ServletRequest> getRequestProvider() {
    return requestProvider;
  }

  public void setRequestProvider(Supplier<ServletRequest> requestProvider) {
    this.requestProvider = requestProvider;
  }

  public Supplier<ServletResponse> getResponseProvider() {
    return responseProvider;
  }

  public void setResponseProvider(Supplier<ServletResponse> responseProvider) {
    this.responseProvider = responseProvider;
  }

  public Supplier<ServletContext> getContextProvider() {
    return contextProvider;
  }

  public void setContextProvider(Supplier<ServletContext> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void doStart(Environment env) {
    for (Class<? extends Annotation> cl : requestAnnotations) {
      env.addProvider(ServletRequest.class, ServletRequest.class, new Annotation[0],
          (AbstractProvider) new SupplierProvider<ServletRequest>(ServletRequest.class, () -> {
            ServletRequest req = requestProvider.get();
            if (req != null) {
              return req;
            } else {
              return null;
            }
          }, cl, env));
      env.addProvider(HttpServletRequest.class, HttpServletRequest.class, new Annotation[0],
          (AbstractProvider) new SupplierProvider<HttpServletRequest>(HttpServletRequest.class,
              () -> {
                ServletRequest req = requestProvider.get();
                if (req != null) {
                  return (HttpServletRequest) req;
                } else {
                  return null;
                }
              }, cl, env));
      env.addProvider(ServletResponse.class, ServletResponse.class, new Annotation[0],
          (AbstractProvider) new SupplierProvider<ServletResponse>(ServletResponse.class, () -> {
            ServletResponse resp = responseProvider.get();
            if (resp != null) {
              return resp;
            } else {
              return null;
            }
          }, cl, env));
      env.addProvider(HttpServletResponse.class, HttpServletResponse.class, new Annotation[0],
          (AbstractProvider) new SupplierProvider<HttpServletResponse>(HttpServletResponse.class,
              () -> {
                ServletResponse resp = responseProvider.get();
                if (resp != null) {
                  return (HttpServletResponse) resp;
                } else {
                  return null;
                }
              }, cl, env));
      env.addProviderBuilder(new RequestProviderBuilder(cl));
    }

    for (Class<? extends Annotation> cl : sessionAnnotations) {
      env.addProvider(HttpSession.class, HttpSession.class, new Annotation[0],
          (AbstractProvider) new SupplierProvider<HttpSession>(HttpSession.class, () -> {
            ServletRequest request = requestProvider.get();
            if (request != null) {
              HttpServletRequest req = (HttpServletRequest) request;
              return req.getSession();
            } else {
              return null;
            }
          }, cl, env));
      env.addProviderBuilder(new SessionProviderBuilder(cl));
    }

    for (Class<? extends Annotation> cl : applicationAnnotations) {
      env.addProvider(ServletContext.class, ServletContext.class, new Annotation[0],
          (AbstractProvider) new SupplierProvider<ServletContext>(ServletContext.class, () -> {
            return contextProvider.get();
          }, cl, env));
      env.addProviderBuilder(new ApplicationProviderBuilder(cl));
    }
  }
}
