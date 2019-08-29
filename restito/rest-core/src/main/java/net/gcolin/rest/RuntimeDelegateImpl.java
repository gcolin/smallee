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

import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.util.ParamConverterProviderImpl;

import java.util.ServiceLoader;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * The RuntimeDelegate implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class RuntimeDelegateImpl extends RuntimeDelegate {

  private ParamConverterProviderImpl hdprovider = new ParamConverterProviderImpl(true);

  @Override
  public UriBuilder createUriBuilder() {
    return new UriBuilderImpl();
  }

  @Override
  public ResponseBuilder createResponseBuilder() {
    return new ResponseBuilderImpl(Contexts.instance().get());
  }

  @Override
  public VariantListBuilder createVariantListBuilder() {
    return new VariantListBuilderImpl();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T createEndpoint(Application application, Class<T> endpointType) {
    for (EndPoint e : ServiceLoader.load(EndPoint.class)) {
      if (e.getClass() == endpointType) {
        e.init(application);
        return (T) e;
      }
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
    return hdprovider.getHeaderDelegate(type);
  }

  @Override
  public Builder createLinkBuilder() {
    return new LinkBuilder();
  }

}
