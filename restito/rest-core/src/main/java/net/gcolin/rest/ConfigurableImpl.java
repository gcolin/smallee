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

package net.gcolin.rest;

import java.util.Map;

import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;

/**
 * A Configurable implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class ConfigurableImpl<C extends Configurable<C>> implements Configurable<C> {

  private RestConfiguration restConfiguration;

  public ConfigurableImpl(RestConfiguration restConfiguration) {
    this.restConfiguration = restConfiguration;
  }

  @Override
  public Configuration getConfiguration() {
    return restConfiguration;
  }

  @Override
  public C property(String name, Object value) {
    restConfiguration.putProperty(name, value);
    return (C) this;
  }

  @Override
  public C register(Class<?> componentClass) {
    restConfiguration.getItemsTypes().add(componentClass);
    restConfiguration.addContract(componentClass, 0);
    return (C) this;
  }

  @Override
  public C register(Class<?> componentClass, int priority) {
    restConfiguration.getItemsTypes().add(componentClass);
    restConfiguration.addContract(componentClass, priority);
    return (C) this;
  }

  @Override
  public C register(Class<?> componentClass, Class<?>... contracts) {
    restConfiguration.getItemsTypes().add(componentClass);
    restConfiguration.addContract(componentClass, contracts);
    return (C) this;
  }

  @Override
  public C register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
    restConfiguration.getItemsTypes().add(componentClass);
    restConfiguration.addContract(componentClass, contracts, 0);
    return (C) this;
  }

  @Override
  public C register(Object component) {
    restConfiguration.getObjects().add(component);
    restConfiguration.addContract(component.getClass(), 0);
    return (C) this;
  }

  @Override
  public C register(Object component, int priority) {
    restConfiguration.getObjects().add(component);
    restConfiguration.addContract(component.getClass(), priority);
    return (C) this;
  }

  @Override
  public C register(Object component, Class<?>... contracts) {
    restConfiguration.getObjects().add(component);
    restConfiguration.addContract(component.getClass(), contracts);
    return (C) this;
  }

  @Override
  public C register(Object component, Map<Class<?>, Integer> contracts) {
    restConfiguration.getObjects().add(component);
    restConfiguration.addContract(component.getClass(), contracts, 0);
    return (C) this;
  }
}
