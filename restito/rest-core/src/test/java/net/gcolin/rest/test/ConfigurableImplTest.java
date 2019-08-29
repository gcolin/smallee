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

import net.gcolin.common.lang.Pair;
import net.gcolin.di.core.InjectException;
import net.gcolin.rest.ConfigurableImpl;
import net.gcolin.rest.RestConfiguration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ConfigurableImplTest {

  private RestConfiguration configuration;
  private ConfigurableImpl<?> configurable;

  @Before
  public void before() {
    configuration = new RestConfiguration(RuntimeType.CLIENT);
    configurable = new ConfigurableImpl<>(configuration);
  }

  @Test
  public void getConfigurationTest() {
    Assert.assertTrue(configurable.getConfiguration() == configuration);
  }

  @Test
  public void propertyTest() {
    configurable.property("hello", "world");
    Assert.assertEquals("world", configuration.getProperty("hello"));
    configurable.property("hello", null);
    Assert.assertNull(configuration.getProperty("hello"));
  }

  @Test
  public void registerTest() {
    configurable.register(Pair.class);
    Assert.assertTrue(configuration.getClasses().contains(Pair.class));
    configurable.register(Pair.class);
    Assert.assertTrue(configuration.getClasses().contains(Pair.class));

    configurable.register(Exception.class, 1);
    Assert.assertTrue(configuration.getClasses().contains(Exception.class));
    configurable.register(Exception.class, 1);
    Assert.assertTrue(configuration.getClasses().contains(Exception.class));

    configurable.register(WebApplicationException.class, Exception.class, RuntimeException.class);
    Assert.assertTrue(configuration.getClasses().contains(WebApplicationException.class));
    configurable.register(WebApplicationException.class, Exception.class, RuntimeException.class);
    Assert.assertTrue(configuration.getClasses().contains(WebApplicationException.class));

    configurable.register(BadRequestException.class,
        Collections.singletonMap(WebApplicationException.class, 2));
    Assert.assertTrue(configuration.getClasses().contains(BadRequestException.class));
    configurable.register(BadRequestException.class,
        Collections.singletonMap(WebApplicationException.class, 2));
    Assert.assertTrue(configuration.getClasses().contains(BadRequestException.class));

    Pair<String, String> pair = new Pair<>();
    configurable.register(pair);
    Assert.assertTrue(configuration.getInstances().contains(pair));
    configurable.register(pair);
    Assert.assertTrue(configuration.getInstances().contains(pair));

    Exception ex = new Exception();
    configurable.register(ex, 1);
    Assert.assertTrue(configuration.getInstances().contains(ex));
    configurable.register(ex, 1);
    Assert.assertTrue(configuration.getInstances().contains(ex));

    InjectException iex = new InjectException();
    configurable.register(iex, Exception.class, RuntimeException.class);
    Assert.assertTrue(configuration.getInstances().contains(iex));
    configurable.register(iex, Exception.class, RuntimeException.class);
    Assert.assertTrue(configuration.getInstances().contains(iex));

    InjectException iex2 = new InjectException();
    configurable.register(iex2, Collections.singletonMap(WebApplicationException.class, 2));
    Assert.assertTrue(configuration.getInstances().contains(iex2));
    configurable.register(iex2, Collections.singletonMap(WebApplicationException.class, 2));
    Assert.assertTrue(configuration.getInstances().contains(iex2));
  }

}
