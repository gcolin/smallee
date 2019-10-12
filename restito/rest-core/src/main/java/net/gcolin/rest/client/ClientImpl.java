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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;

import net.gcolin.common.collection.Collections2;
import net.gcolin.common.io.Io;
import net.gcolin.common.reflect.Injector;
import net.gcolin.rest.ConfigurableImpl;
import net.gcolin.rest.Environment;
import net.gcolin.rest.Logs;
import net.gcolin.rest.RestConfiguration;
import net.gcolin.rest.provider.SimpleProviders;

/**
 * The Client implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ClientImpl extends ConfigurableImpl<Client> implements Client {

  private static final Injector[] INJECTORS = Collections2
      .safeFillServiceLoaderAsArray(ClientBuilderImpl.class.getClassLoader(), Injector.class);

  private HostnameVerifier hostnameVerifier;
  private Supplier<SSLContext> sslContext;
  private volatile boolean closed = false;
  private Set<AutoCloseable> closeable =
      Collections.newSetFromMap(new ConcurrentHashMap<AutoCloseable, Boolean>());
  private Environment environment = new Environment();
  private List<NewCookie> cookies = Collections.synchronizedList(new ArrayList<>());
  private ExecutorService asyncInvocationExecutor;
  private boolean closeExecutor;

  ClientImpl(RestConfiguration restConfiguration, HostnameVerifier hostnameVerifier,
      Supplier<SSLContext> sslContext) {
    super(restConfiguration);
    this.hostnameVerifier = hostnameVerifier;
    this.sslContext = sslContext;
  }

  public List<NewCookie> getCookies() {
    return cookies;
  }

  synchronized ExecutorService getAsyncInvocationExecutor() {
    if (asyncInvocationExecutor == null) {
      // try to get ExecutorService from an injection framework
      for (int i = 0; i < INJECTORS.length && asyncInvocationExecutor == null; i++) {
        try {
          asyncInvocationExecutor = INJECTORS[i].get(ThreadPoolExecutor.class);
        } catch (Exception ex) {
          Logs.LOG.log(Level.FINE, "cannot retrieve ThreadPoolExecutor from injector", ex);
        }
      }
      if (asyncInvocationExecutor == null) {
        closeExecutor = true;
        asyncInvocationExecutor = Executors.newSingleThreadExecutor();
      }
    }
    return asyncInvocationExecutor;
  }

  public Environment getEnvironment() {
    return environment;
  }

  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  public void add(AutoCloseable cl) {
    closeable.add(cl);
  }

  public void remove(AutoCloseable cl) {
    closeable.remove(cl);
  }

  /**
   * Check that the client is open.
   */
  public void checkOpen() {
    if (closed) {
      throw new IllegalStateException("client closed");
    }
  }

  @Override
  public void close() {
    if (!closed) {
      closed = true;
      Set<AutoCloseable> sc = null;
      while (!(sc = new HashSet<>(closeable)).isEmpty()) {
        sc.forEach(Io::close);
        closeable.removeAll(sc);
      }
      if (closeExecutor && asyncInvocationExecutor != null) {
        asyncInvocationExecutor.shutdown();
      }
    }
  }

  public boolean isClosed() {
    return closed;
  }

  @Override
  public WebTarget target(String uri) {
    checkOpen();
    return target(UriBuilder.fromUri(uri));
  }

  @Override
  public WebTarget target(URI uri) {
    return target(UriBuilder.fromUri(uri));
  }

  @Override
  public WebTarget target(UriBuilder uriBuilder) {
    checkOpen();
    return new WebTargetImpl(uriBuilder, this, (RestConfiguration) getConfiguration());
  }

  @Override
  public WebTarget target(Link link) {
    return target(UriBuilder.fromLink(link));
  }

  @Override
  public Builder invocation(Link link) {
    checkOpen();
    return new BuilderImpl(UriBuilder.fromLink(link), this, getConfiguration().getProperties(),
        newClientFeatureBuilder((RestConfiguration) getConfiguration()));
  }

  @Override
  public SSLContext getSslContext() {
    return sslContext.get();
  }

  @Override
  public HostnameVerifier getHostnameVerifier() {
    return hostnameVerifier;
  }

  ClientFeatureBuilder newClientFeatureBuilder(RestConfiguration configuration) {
    SimpleProviders providers = new SimpleProviders(RuntimeType.CLIENT);
    providers.load();
    ClientFeatureBuilder builder = new ClientFeatureBuilder(providers, configuration, environment);
    builder.build();
    providers.flush(environment);
    return builder;
  }

}
