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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

/**
 * A Configuration implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class RestConfiguration implements Configuration {

  private static final Class<?>[] SERVER_CONTRACTS =
      {ExceptionMapper.class, MessageBodyWriter.class, MessageBodyReader.class,
          ContextResolver.class, Feature.class, DynamicFeature.class, ParamConverterProvider.class,
          ContainerRequestFilter.class, ContainerResponseFilter.class, WriterInterceptor.class,
          ReaderInterceptor.class, Supplier.class};
  private static final Class<?>[] CLIENT_CONTRACTS =
      {ExceptionMapper.class, MessageBodyWriter.class, MessageBodyReader.class,
          ContextResolver.class, Feature.class, Supplier.class, ClientRequestFilter.class,
          ClientResponseFilter.class, WriterInterceptor.class, ReaderInterceptor.class};
  private Map<String, Object> properties = new HashMap<>();
  private Set<Class<?>> itemsTypes = new HashSet<>();
  private Set<Object> objects = new HashSet<>();
  private Set<Class<?>> itemsTypesI = Collections.unmodifiableSet(itemsTypes);
  private Set<Object> objectsI = Collections.unmodifiableSet(objects);
  private Map<Class<?>, Map<Class<?>, Integer>> contracts = new HashMap<>();
  private RuntimeType runtimeType;

  public RestConfiguration(RuntimeType runtimeType) {
    this.runtimeType = runtimeType;
  }

  public RestConfiguration newInstance() {
    return new RestConfiguration(runtimeType).withConfig(this);
  }

  @Override
  public RuntimeType getRuntimeType() {
    return runtimeType;
  }

  @Override
  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public Object getProperty(String name) {
    return properties.get(name);
  }

  @Override
  public Collection<String> getPropertyNames() {
    return properties.keySet();
  }

  @Override
  public boolean isEnabled(Feature feature) {
    return objects.contains(feature);
  }

  @Override
  public boolean isEnabled(Class<? extends Feature> featureClass) {
    return itemsTypes.contains(featureClass);
  }

  @Override
  public boolean isRegistered(Object component) {
    return objects.contains(component);
  }

  @Override
  public boolean isRegistered(Class<?> componentClass) {
    return itemsTypes.contains(componentClass);
  }

  @Override
  public Map<Class<?>, Integer> getContracts(Class<?> componentClass) {
    Map<Class<?>, Integer> contractMap = contracts.get(componentClass);
    return contractMap == null ? Collections.emptyMap() : contractMap;
  }

  public Map<Class<?>, Map<Class<?>, Integer>> getContracts() {
    return contracts;
  }

  @Override
  public Set<Class<?>> getClasses() {
    return itemsTypesI;
  }

  @Override
  public Set<Object> getInstances() {
    return objectsI;
  }

  public Set<Class<?>> getItemsTypes() {
    return itemsTypes;
  }

  public Set<Object> getObjects() {
    return objects;
  }

  /**
   * Add a property. A null value remove the property.
   * 
   * @param name the name of property
   * @param value the value of property
   */
  public void putProperty(String name, Object value) {
    if (value == null) {
      properties.remove(name);
    } else {
      properties.put(name, value);
    }
  }

  /**
   * Reconfigure with another configuration.
   * 
   * @param config another configuration
   * @return this configuration updated
   */
  public RestConfiguration withConfig(Configuration config) {
    runtimeType = config.getRuntimeType();
    itemsTypes.clear();
    objects.clear();
    properties.clear();
    contracts.clear();
    properties.putAll(config.getProperties());
    objects.addAll(config.getInstances());
    itemsTypes.addAll(config.getClasses());
    for (Class<?> c : itemsTypes) {
      addContract(c, config.getContracts(c), 0);
    }
    for (Object o : objects) {
      addContract(o.getClass(), config.getContracts(o.getClass()), 0);
    }
    return this;
  }


  /**
   * Add contracts or priority to a bean.
   * 
   * @param beanType type of bean
   * @param contractMap map of contracts
   * @param priority priority of the bean
   */
  public void addContract(Class<?> beanType, Map<Class<?>, Integer> contractMap, int priority) {
    if (contracts == null) {
      addContract(beanType, priority);
    } else {
      contracts.put(beanType, contractMap);
    }
  }

  /**
   * Add contracts to a bean.
   * 
   * @param beanType type of bean
   * @param contractClasses contracts
   */
  public void addContract(Class<?> beanType, Class<?>[] contractClasses) {
    Map<Class<?>, Integer> contract = getContractsByType(beanType);
    for (int i = 0; i < contractClasses.length; i++) {
      contract.put(contractClasses[i], 0);
    }
  }
  
  /**
   * Add priority to a bean.
   * 
   * @param beanType type of bean
   * @param priority priority of the bean
   */
  public void addContract(Class<?> beanType, int priority) {
    Map<Class<?>, Integer> contract = getContractsByType(beanType);
    Class<?>[] possibles =
        getRuntimeType() == RuntimeType.SERVER ? SERVER_CONTRACTS : CLIENT_CONTRACTS;
    for (int i = 0; i < possibles.length; i++) {
      if (possibles[i].isAssignableFrom(beanType)) {
        contract.put(possibles[i], priority);
      }
    }
  }

  private Map<Class<?>, Integer> getContractsByType(Class<?> beanType) {
    Map<Class<?>, Integer> contract = contracts.get(beanType);
    if (contract == null) {
      contract = new HashMap<>();
      contracts.put(beanType, contract);
    }
    return contract;
  }

}
