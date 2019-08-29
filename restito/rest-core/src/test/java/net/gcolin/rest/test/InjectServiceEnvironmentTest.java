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

import net.gcolin.di.core.InjectService;
import net.gcolin.rest.InjectServiceEnvironment;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InjectServiceEnvironmentTest {

  private boolean put;

  public static class A {

  }

  public static class B {

  }

  @Test
  public void createProviderTest() {
    InjectService service = Mockito.mock(InjectService.class);
    Mockito.when(service.findSupplier(A.class)).thenReturn(() -> new A());

    InjectServiceEnvironment env = new InjectServiceEnvironment(service);
    Assert.assertTrue(env.createProvider(A.class, A.class, false).get() instanceof A);
  }

  @Test
  public void putTest() {
    InjectService service = Mockito.mock(InjectService.class);
    Mockito.doAnswer(new Answer<Object>() {

      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        put = true;
        return null;
      }
    }).when(service).bind(Mockito.anyObject());

    InjectServiceEnvironment env = new InjectServiceEnvironment(service);
    env.put(new A());
    Assert.assertTrue(put);
  }

  @Test
  public void isMutableTest() {
    InjectService service = Mockito.mock(InjectService.class);
    Mockito.when(service.isMutable(A.class)).thenReturn(true);
    Mockito.when(service.isMutable(B.class)).thenReturn(false);

    InjectServiceEnvironment env = new InjectServiceEnvironment(service);
    Assert.assertTrue(env.isMutable(A.class));
    Assert.assertFalse(env.isMutable(B.class));
  }

}
