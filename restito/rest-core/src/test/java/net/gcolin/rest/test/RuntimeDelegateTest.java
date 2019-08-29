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

import net.gcolin.rest.EndPoint;
import net.gcolin.rest.RuntimeDelegateImpl;

import org.junit.Assert;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class RuntimeDelegateTest {

  public static class EndPoint3 implements EndPoint {

    @Override
    public void init(Application app) {

    }

  }

  @org.junit.Test
  public void testFind() {
    Assert.assertEquals(RuntimeDelegateImpl.class, RuntimeDelegate.getInstance().getClass());

    Assert.assertNotNull(RuntimeDelegate.getInstance().createHeaderDelegate(String.class));
    Assert.assertNotNull(RuntimeDelegate.getInstance().createUriBuilder());
    Assert.assertNotNull(RuntimeDelegate.getInstance().createLinkBuilder());

    Application app = new Application();
    EndPoint2 e2 = RuntimeDelegate.getInstance().createEndpoint(app, EndPoint2.class);
    Assert.assertEquals(app, e2.app);

    try {
      RuntimeDelegate.getInstance().createEndpoint(new Application(), EndPoint3.class);
      Assert.fail();
    } catch (UnsupportedOperationException ex) {
      // ok
    }
  }
}
