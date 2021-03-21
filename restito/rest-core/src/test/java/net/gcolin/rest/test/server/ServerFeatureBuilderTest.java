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

package net.gcolin.rest.test.server;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import net.gcolin.rest.Environment;
import net.gcolin.rest.RestConfiguration;
import net.gcolin.rest.router.Router;
import net.gcolin.rest.router.RouterResponse;
import net.gcolin.rest.server.AbstractResource;
import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.server.ResourceArray;
import net.gcolin.rest.server.RestContainer;
import net.gcolin.rest.server.ServerFeatureBuilder;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.server.ServerProviders;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServerFeatureBuilderTest {

  private static boolean filtercalled = false;
  private static boolean servicecalled = false;

  @FilterBinding
  public static class MyFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
      Assert.assertFalse(servicecalled);
      filtercalled = true;
    }
  }

  @Path("hello")
  @FilterBinding
  public static class Service {

    @GET
    public void call() {
      servicecalled = true;
    }

  }

  @Test
  public void testContainerFilter() throws IOException {
    Router<ResourceArray> router = new Router<>();
    RestConfiguration configuration = new RestConfiguration(RuntimeType.SERVER);
    
    ServerFeatureBuilder sb = new ServerFeatureBuilder(Mockito.mock(RestContainer.class),
        new ServerProviders(), router, configuration, new Environment());

    sb.register(new MyFilter());
    sb.register(new Service());
    
    sb.build();

    RouterResponse<ResourceArray> ra = router.get("hello");
    Assert.assertNotNull(ra);
    Assert.assertNotNull(ra.getResult());
    Assert.assertNotNull(ra.getResult().get(ResourceArray.GET));
    AbstractResource ar = ra.getResult().get(ResourceArray.GET).select(null);
    Assert.assertNotNull(ar);
    filtercalled = false;
    servicecalled = false;
    ServerInvocationContext ctx = Mockito.mock(ServerInvocationContext.class);
    Contexts.instance().set(ctx);
    try {
      ar.handle(ctx);
      Assert.assertTrue(servicecalled);
      Assert.assertTrue(filtercalled);
    } finally {
      Contexts.instance().remove();
    }
  }

}
