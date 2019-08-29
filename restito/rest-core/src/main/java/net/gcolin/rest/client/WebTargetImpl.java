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

import net.gcolin.rest.ConfigurableImpl;
import net.gcolin.rest.RestConfiguration;

import java.net.URI;
import java.util.Map;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * The WebTarget implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
class WebTargetImpl extends ConfigurableImpl<WebTarget> implements WebTarget {

  private UriBuilder uriBuilder;
  private ClientImpl client;

  public WebTargetImpl(UriBuilder uriBuilder, ClientImpl client, RestConfiguration configuration) {
    super(configuration.newInstance());
    this.uriBuilder = uriBuilder;
    this.client = client;
  }

  @Override
  public URI getUri() {
    return uriBuilder.build();
  }

  @Override
  public UriBuilder getUriBuilder() {
    client.checkOpen();
    return uriBuilder;
  }

  @Override
  public WebTarget path(String path) {
    return newInstance(uriBuilder.clone().path(path));
  }

  @Override
  public WebTarget resolveTemplate(String name, Object value) {
    return newInstance(uriBuilder.clone().resolveTemplate(name, value));
  }

  @Override
  public WebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
    return newInstance(uriBuilder.clone().resolveTemplate(name, value, encodeSlashInPath));
  }

  @Override
  public WebTarget resolveTemplateFromEncoded(String name, Object value) {
    return newInstance(uriBuilder.clone().resolveTemplateFromEncoded(name, value));
  }

  @Override
  public WebTarget resolveTemplates(Map<String, Object> templateValues) {
    return newInstance(uriBuilder.clone().resolveTemplates(templateValues));
  }

  @Override
  public WebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath) {
    return newInstance(uriBuilder.clone().resolveTemplates(templateValues, encodeSlashInPath));
  }

  @Override
  public WebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
    return newInstance(uriBuilder.clone().resolveTemplatesFromEncoded(templateValues));
  }

  @Override
  public WebTarget matrixParam(String name, Object... values) {
    return newInstance(uriBuilder.clone().matrixParam(name, values));
  }

  @Override
  public WebTarget queryParam(String name, Object... values) {
    return newInstance(uriBuilder.clone().queryParam(name, values));
  }

  private WebTarget newInstance(UriBuilder uriBuilder) {
    client.checkOpen();
    return new WebTargetImpl(uriBuilder, client, (RestConfiguration) getConfiguration());
  }

  @Override
  public Builder request() {
    client.checkOpen();
    RestConfiguration configuration = (RestConfiguration) getConfiguration();
    return new BuilderImpl(uriBuilder, client, configuration.getProperties(),
        client.newClientFeatureBuilder(configuration));
  }

  @Override
  public Builder request(String... acceptedResponseTypes) {
    Builder builder = request();
    builder.accept(acceptedResponseTypes);
    return builder;
  }

  @Override
  public Builder request(MediaType... acceptedResponseTypes) {
    Builder builder = request();
    builder.accept(acceptedResponseTypes);
    return builder;
  }

}
