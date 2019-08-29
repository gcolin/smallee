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

package net.gcolin.rest.test.client;

import static org.junit.Assert.assertEquals;

import net.gcolin.common.lang.Pair;
import net.gcolin.rest.Environment;
import net.gcolin.rest.RestConfiguration;
import net.gcolin.rest.client.ClientFeatureBuilder;
import net.gcolin.rest.provider.SimpleProviders;

import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ClientFeatureBuilderTest {

  public static class PairProvider implements Supplier<Pair<String, String>> {

    @Override
    public Pair<String, String> get() {
      return new Pair<>("hello", "world");
    }

  }

  public static class ClientResponseFilterImpl implements ClientResponseFilter {

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext)
        throws IOException {}

  }

  public static class ClientRequestFilterImpl implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {}

  }

  public static class WriterInterceptorImpl implements WriterInterceptor {

    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
        throws IOException, WebApplicationException {}

  }

  public static class ReaderInterceptorImpl implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context)
        throws IOException, WebApplicationException {
      return context.proceed();
    }

  }

  public static class InterceptorFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
      context.register(ReaderInterceptorImpl.class);
      context.register(new WriterInterceptorImpl());
      return true;
    }

  }

  public static class FilterFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
      context.register(ClientRequestFilterImpl.class);
      context.register(new ClientResponseFilterImpl());
      context.register(InterceptorFeature.class);
      return true;
    }

  }

  @Test
  public void simpleTest() {
    SimpleProviders providers = new SimpleProviders(RuntimeType.SERVER);
    providers.load();
    ClientFeatureBuilder builder = new ClientFeatureBuilder(providers,
        new RestConfiguration(RuntimeType.CLIENT), new Environment());

    builder.register(new FilterFeature());

    builder.register(PairProvider.class);

    builder.build();

    assertEquals(1, builder.getClientRequestFilters().size());
    assertEquals(1, builder.getClientResponseFilters().size());
    assertEquals(1, builder.getReaderInterceptors().size());
    assertEquals(1, builder.getWriterInterceptors().size());

  }

}
