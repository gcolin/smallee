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

package net.gcolin.rest.client;

import net.gcolin.rest.Environment;
import net.gcolin.rest.FeatureBuilder;
import net.gcolin.rest.RestConfiguration;
import net.gcolin.rest.provider.SimpleProviders;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

/**
 * A class for enabling all features of a REST client.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ClientFeatureBuilder extends FeatureBuilder implements FeatureContext {

  private List<ClientResponseFilter> clientResponseFilters = new ArrayList<>();
  private List<ClientRequestFilter> clientRequestFilters = new ArrayList<>();
  private List<WriterInterceptor> writerInterceptors = new ArrayList<>();
  private List<ReaderInterceptor> readerInterceptors = new ArrayList<>();

  public ClientFeatureBuilder(SimpleProviders providers, RestConfiguration restConfiguration,
      Environment environment) {
    super(restConfiguration, providers, environment);
  }

  @Override
  protected void enableExtras() {
    // no specific resources supported
  }

  @Override
  protected void enableContainerFilters() {
    clientResponseFilters.addAll(getInstances(ClientResponseFilter.class));
    clientRequestFilters.addAll(getInstances(ClientRequestFilter.class));
  }

  @Override
  protected void enableInterceptors() {
    writerInterceptors.addAll(getInstances(WriterInterceptor.class));
    readerInterceptors.addAll(getInstances(ReaderInterceptor.class));
  }

  public List<ClientRequestFilter> getClientRequestFilters() {
    return clientRequestFilters;
  }

  public List<ClientResponseFilter> getClientResponseFilters() {
    return clientResponseFilters;
  }

  public List<ReaderInterceptor> getReaderInterceptors() {
    return readerInterceptors;
  }

  public List<WriterInterceptor> getWriterInterceptors() {
    return writerInterceptors;
  }
}
