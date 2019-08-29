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

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

/**
 * A fluent class for creating a REST service.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Builder {

  private static final String[] DEFAULT_MEDIA_TYPE = {MediaType.WILDCARD};
  private String[] methods;
  private String[] produces = DEFAULT_MEDIA_TYPE;
  private String[] consumes = new String[0];
  private String path;
  private ResponseFunction handler;
  private ResponseSupplier handler2;
  private Object instance;
  private Method method;

  public Builder() {

  }

  public Builder method(String... methods) {
    this.methods = methods;
    return this;
  }

  public Builder consumes(String... consumes) {
    this.consumes = consumes;
    return this;
  }

  public Builder produces(String... produces) {
    this.produces = produces;
    return this;
  }

  public Builder path(String path) {
    this.path = path;
    return this;
  }

  public Builder handle(ResponseFunction handler) {
    this.handler = handler;
    return this;
  }

  public Builder handle(ResponseSupplier handler2) {
    this.handler2 = handler2;
    return this;
  }

  /**
   * Add a method to call and an instance.
   * 
   * @param instance the instance of the resource
   * @param method the method of the resource
   * @return the current builder
   */
  public Builder handle(Object instance, Method method) {
    this.instance = instance;
    this.method = method;
    return this;
  }

  public Application build() {
    return new App(this);
  }

  public String[] getMethods() {
    return methods;
  }

  public String[] getProduces() {
    return produces;
  }

  public String[] getConsumes() {
    return consumes;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Get the response supplier.
   * 
   * @return the response supplier or {@code null}
   */
  public ResponseSupplier getSupplier() {
    if (handler2 != null) {
      return handler2;
    } else if (handler != null) {
      return () -> handler.apply(Contexts.instance().get());
    }
    return null;
  }

  public Method getMethod() {
    return method;
  }

  public Object getInstance() {
    return instance;
  }

  private static class App extends Application {
    private Builder builder;

    public App(Builder builder) {
      this.builder = builder;
    }

    public Set<Object> getSingletons() {
      Set<Object> set = new HashSet<Object>();
      set.add(builder);
      return set;
    }
  }
}
