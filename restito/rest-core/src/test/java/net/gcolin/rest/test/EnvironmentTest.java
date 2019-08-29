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

package net.gcolin.rest.test;

import net.gcolin.rest.Environment;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.server.ServerInvocationContext;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class EnvironmentTest {

  public static class A {

  }

  public static class B implements ContainerRequestFilter {
    @Context
    UriInfo info;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {}
  }

  @Test
  public void noInjectionTest() {
    Environment env = new Environment();

    Supplier<Object> supplier = env.getProvider(A.class, A.class, true);
    Assert.assertTrue(supplier == env.getProvider(A.class, A.class, true));
    Assert.assertFalse(Proxy.isProxyClass(supplier.get().getClass()));
    Assert.assertTrue(supplier.get() instanceof A);
  }

  @Test
  public void noProxyInjectionTest() {
    ServerInvocationContext ctx = Mockito.mock(ServerInvocationContext.class);
    UriInfo info = Mockito.mock(UriInfo.class);
    Mockito.when(ctx.getUriInfo()).thenReturn(info);
    Contexts.instance().set(ctx);

    SimpleProviders sp = new SimpleProviders(RuntimeType.SERVER);
    sp.load();
    Environment env = new Environment();
    env.setProviders(sp);

    Supplier<Object> supplier = env.getProvider(B.class, ContainerRequestFilter.class, false);
    Assert.assertTrue(supplier == env.getProvider(B.class, ContainerRequestFilter.class, false));
    Assert.assertFalse(Proxy.isProxyClass(supplier.get().getClass()));
    Assert.assertTrue(supplier.get() instanceof B);
    Assert.assertTrue(((B) supplier.get()).info == info);
  }

  @Test
  public void proxyInjectionTest() {
    ServerInvocationContext ctx = Mockito.mock(ServerInvocationContext.class);
    UriInfo info = Mockito.mock(UriInfo.class);
    Mockito.when(ctx.getUriInfo()).thenReturn(info);
    Contexts.instance().set(ctx);

    SimpleProviders sp = new SimpleProviders(RuntimeType.SERVER);
    sp.load();
    Environment env = new Environment();
    env.setProviders(sp);

    Supplier<Object> supplier = env.getProvider(B.class, ContainerRequestFilter.class, true);
    Assert.assertTrue(supplier == env.getProvider(B.class, ContainerRequestFilter.class, true));
    Assert.assertTrue(Proxy.isProxyClass(supplier.get().getClass()));
    Assert.assertTrue(supplier.get() instanceof ContainerRequestFilter);
  }

}
