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

package net.gcolin.rest.server.test;

import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.servlet.ServletContextParam;
import net.gcolin.rest.servlet.ServletExchange;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServletContextParamTest {

  @Test
  public void updateTest() {
    ServerInvocationContext rc = Mockito.mock(ServerInvocationContext.class);
    ServletExchange exchange = Mockito.mock(ServletExchange.class);
    Mockito.when(rc.getExchange()).thenReturn(exchange);
    HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
    Mockito.when(exchange.getRequest()).thenReturn(req);
    ServletContext ctx = Mockito.mock(ServletContext.class);
    Mockito.when(req.getServletContext()).thenReturn(ctx);

    ServletContextParam param = new ServletContextParam();
    Assert.assertEquals(ctx, param.update(rc));
  }

}
