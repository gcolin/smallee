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
import net.gcolin.rest.util.SslConfigurator;

import java.security.KeyStore;
import java.util.Map;
import java.util.function.Supplier;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

/**
 * The ClientBuilder implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ClientBuilderImpl extends ClientBuilder {

  private RestConfiguration configuration = new RestConfiguration(RuntimeType.CLIENT);
  private ConfigurableImpl<ClientBuilder> configurable = new ConfigurableImpl<>(configuration);
  private HostnameVerifier hostnameVerifier;
  private SSLContext sslContext;
  private SslConfigurator sslConfigurator;

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public ClientBuilder property(String name, Object value) {
    configuration.putProperty(name, value);
    return this;
  }

  @Override
  public ClientBuilder register(Class<?> componentClass) {
    configurable.register(componentClass);
    return this;
  }

  @Override
  public ClientBuilder register(Class<?> componentClass, int priority) {
    configurable.register(componentClass, priority);
    return this;
  }

  @Override
  public ClientBuilder register(Class<?> componentClass, Class<?>... contracts) {
    configurable.register(componentClass, contracts);
    return this;
  }

  @Override
  public ClientBuilder register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
    configurable.register(componentClass, contracts);
    return this;
  }

  @Override
  public ClientBuilder register(Object component) {
    configurable.register(component);
    return this;
  }

  @Override
  public ClientBuilder register(Object component, int priority) {
    configurable.register(component, priority);
    return this;
  }

  @Override
  public ClientBuilder register(Object component, Class<?>... contracts) {
    configurable.register(component, contracts);
    return this;
  }

  @Override
  public ClientBuilder register(Object component, Map<Class<?>, Integer> contracts) {
    configurable.register(component, contracts);
    return this;
  }

  @Override
  public ClientBuilder withConfig(Configuration config) {
    configuration.withConfig(config);
    return this;
  }

  @Override
  public ClientBuilder sslContext(SSLContext sslContext) {
    this.sslContext = sslContext;
    sslConfigurator = null;
    return this;
  }

  @Override
  public ClientBuilder keyStore(KeyStore keyStore, char[] password) {
    if (keyStore == null) {
      throw new NullPointerException("null keystore");
    }
    if (password == null) {
      throw new NullPointerException("null password");
    }
    if (sslConfigurator == null) {
      sslConfigurator = SslConfigurator.newInstance();
    }
    sslConfigurator.keyStore(keyStore);
    sslConfigurator.keyPassword(password);
    sslContext = null;
    return this;
  }

  @Override
  public ClientBuilder trustStore(KeyStore trustStore) {
    if (trustStore == null) {
      throw new NullPointerException("null keystore");
    }
    if (sslConfigurator == null) {
      sslConfigurator = SslConfigurator.newInstance();
    }
    sslConfigurator.trustStore(trustStore);
    sslContext = null;
    return this;
  }

  @Override
  public ClientBuilder hostnameVerifier(HostnameVerifier verifier) {
    this.hostnameVerifier = verifier;
    return this;
  }

  @Override
  public Client build() {
    Supplier<SSLContext> sslContextSupplier;
    if (sslConfigurator != null) {
      SslConfigurator copy = sslConfigurator.copy();
      sslContextSupplier = new Supplier<SSLContext>() {

        private SSLContext context;

        @Override
        public SSLContext get() {
          if (context == null) {
            context = copy.createSslContext();
          }
          return context;
        }
      };
    } else {
      sslContextSupplier = () -> sslContext;
    }

    return new ClientImpl(configuration.newInstance(), hostnameVerifier, sslContextSupplier);
  }

}
