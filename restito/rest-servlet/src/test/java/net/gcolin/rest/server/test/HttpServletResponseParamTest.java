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
import net.gcolin.rest.servlet.HttpResponseObserver;
import net.gcolin.rest.servlet.HttpServletResponseParam;
import net.gcolin.rest.servlet.ServletExchange;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class HttpServletResponseParamTest {

  @Test
  public void updateTest() {
    ServerInvocationContext rc = Mockito.mock(ServerInvocationContext.class);
    ServletExchange exchange = Mockito.mock(ServletExchange.class);
    Mockito.when(rc.getExchange()).thenReturn(exchange);
    HttpResponseObserver resp = Mockito.mock(HttpResponseObserver.class);
    Mockito.when(exchange.getResponseObserver()).thenReturn(resp);

    HttpServletResponseParam param = new HttpServletResponseParam();
    Assert.assertEquals(resp, param.update(rc));
  }

}
