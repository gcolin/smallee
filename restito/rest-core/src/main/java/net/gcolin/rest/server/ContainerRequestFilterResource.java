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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import net.gcolin.rest.MessageBodyWriterDecorator;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.util.Filters;

/**
 * A resource filtered by ContainerRequestFilters.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ContainerRequestFilterResource extends AbstractResource {

  private AbstractResource delegate;
  private ContainerRequestFilter[] filters = null;
  private SimpleProviders providers;

  /**
   * Create a ContainerRequestFilterResource.
   * 
   * @param delegate a delegate resource
   * @param providers the providers helper
   */
  public ContainerRequestFilterResource(AbstractResource delegate, SimpleProviders providers) {
    this.delegate = delegate;
    this.providers = providers;
    delegate.bind(this);
  }

  /**
   * Add a filter.
   * 
   * @param filter a filter to add
   * @return the current ContainerRequestFilterResource
   */
  public ContainerRequestFilterResource add(ContainerRequestFilter filter) {
    delegate.injectConsumer(filter);
    if (filters == null) {
      filters = new ContainerRequestFilter[] {filter};
    } else {
      ContainerRequestFilter[] tmp = new ContainerRequestFilter[filters.length + 1];
      System.arraycopy(filters, 0, tmp, 0, filters.length);
      tmp[filters.length] = filter;
      filters = tmp;
      Arrays.sort(filters, Filters.SORT);
    }
    return this;
  }

  public SimpleProviders getProviders() {
    return providers;
  }

  @Override
  public Method getResourceMethod() {
    return delegate.getResourceMethod();
  }

  @Override
  public Class<?> getResourceClass() {
    return delegate.getResourceClass();
  }

  @Override
  public String getPath() {
    return delegate.getPath();
  }

  @Override
  public Class<?> getSource() {
    return delegate.getSource();
  }

  @Override
  public Response handle(ServerInvocationContext ctx) throws IOException {
    for (int i = 0; i < filters.length; i++) {
      filters[i].filter(ctx);
      if (ctx.getAbortResponse() != null) {
        return ctx.getAbortResponse();
      }
    }

    return delegate.handle(ctx);
  }

  @Override
  public Annotation[] getAnnotations() {
    return delegate.getAnnotations();
  }

  @Override
  public AbstractResource addInterceptor(WriterInterceptor wi) {
    delegate.addInterceptor(wi);
    return this;
  }

  @Override
  public AbstractResource addInterceptor(ReaderInterceptor ri) {
    delegate.addInterceptor(ri);
    return this;
  }

  @Override
  public AbstractResource addFilter(ContainerResponseFilter rf) {
    return delegate.addFilter(rf);
  }

  @Override
  public Supplier<Object> getInstance() {
    return delegate.getInstance();
  }

  @Override
  public void setParamValidator(Consumer<Object[]> paramValidator) {
    delegate.setParamValidator(paramValidator);
  }

  @Override
  public AbstractResource select(ServerInvocationContext context) {
    return delegate.select(context) == null ? null : this;
  }

  @Override
  public MessageBodyWriterDecorator getWriterDecorator() {
    return delegate.getWriterDecorator();
  }

  @Override
  public List<ContainerResponseFilter> getResponseFilters() {
    return delegate.getResponseFilters();
  }

  @Override
  public Set<String> getAllowedMethods() {
    return delegate.getAllowedMethods();
  }
}
