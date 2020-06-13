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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import net.gcolin.rest.Logs;
import net.gcolin.rest.MessageBodyWriterDecorator;
import net.gcolin.rest.util.HasPath;

/**
 * A partial server REST service.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class AbstractResource extends ResourceSelector implements ResourceInfo, HasPath {

  public abstract AbstractResource addInterceptor(WriterInterceptor wi);

  public abstract AbstractResource addInterceptor(ReaderInterceptor ri);

  public abstract AbstractResource addFilter(ContainerResponseFilter rf);

  public abstract void setParamValidator(Consumer<Object[]> paramValidator);

  public abstract Class<?> getSource();

  public abstract Response handle(ServerInvocationContext ctx) throws IOException;

  public abstract Annotation[] getAnnotations();

  public abstract Supplier<Object> getInstance();

  public abstract MessageBodyWriterDecorator getWriterDecorator();

  public abstract List<ContainerResponseFilter> getResponseFilters();

  public abstract Set<String> getAllowedMethods();

  /**
   * Inspect the current resource.
   * 
   * @param wi an inspector
   */
  @SuppressWarnings("unchecked")
  public void injectConsumer(Object wi) {
    try {
      if (wi instanceof Consumer && wi.getClass().getMethod("accept", ResourceInfo.class) != null) {
        ((Consumer<ResourceInfo>) wi).accept(this);
      }
    } catch (NoSuchMethodException | SecurityException ex) {
      if (Logs.LOG.isTraceEnabled()) {
        Logs.LOG.trace("cannot inject ResourceInfo in " + wi, ex);
      }
    }
  }
}
