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

package net.gcolin.rest.servlet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * A composite application.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CompositeApplication extends Application {

  private final List<Application> apps;

  public CompositeApplication(List<Application> apps) {
    this.apps = apps;
  }

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<>();
    for (Application app : apps) {
      classes.addAll(app.getClasses());
    }
    return classes;
  }

  @Override
  public Map<String, Object> getProperties() {
    Map<String, Object> props = new HashMap<>();
    for (Application app : apps) {
      props.putAll(app.getProperties());
    }
    return props;
  }

  @Override
  public Set<Object> getSingletons() {
    Set<Object> objects = new HashSet<>();
    for (Application app : apps) {
      objects.addAll(app.getSingletons());
    }
    return objects;
  }

}
