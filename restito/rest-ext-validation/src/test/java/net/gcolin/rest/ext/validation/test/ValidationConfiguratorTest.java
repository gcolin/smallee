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

package net.gcolin.rest.ext.validation.test;

import net.gcolin.rest.ext.validation.ValidationConfigurator;
import net.gcolin.rest.ext.validation.ValidationContainerRequestFilter;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.FeatureContext;

/**
 * a test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ValidationConfiguratorTest {

  @Test
  public void configureTest() {
    new ValidationConfigurator().configure(null);
  }

  @Test
  public void configureFeatureTest() {
    FeatureContext fc = Mockito.mock(FeatureContext.class);
    List<Object> list = new ArrayList<>();
    Mockito.when(fc.register(Mockito.any(Object.class))).then(new Answer<FeatureContext>() {

      @Override
      public FeatureContext answer(InvocationOnMock invocation) throws Throwable {
        list.add(invocation.getArguments()[0]);
        return fc;
      }
    });

    new ValidationConfigurator().configureFeature(fc);

    Assert.assertEquals(1, list.size());
    Assert.assertTrue(list.get(0) instanceof ValidationContainerRequestFilter);
  }

}
