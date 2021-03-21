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

package net.gcolin.rest.server;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.gcolin.rest.Environment;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.router.Router;

/**
 * Internal data for building a REST Service.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ResourceBuilderContext {

  private Method method;
  private Method currentMethod;
  private List<FastMediaType> produces;
  private Set<FastMediaType> consumes;
  private String methodPath;
  private Class<?> clazz;
  private Class<?> currentClazz;
  private String rootPath;
  private Router<ResourceArray> router;
  private Set<String> methodsWithResources;
  private Environment env;
  private Object singleton;
  private Set<String> httpMethods;
  private final List<AbstractResource> resources = new ArrayList<>();

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public Method getCurrentMethod() {
    return currentMethod;
  }

  public void setCurrentMethod(Method currentMethod) {
    this.currentMethod = currentMethod;
  }

  public List<FastMediaType> getProduces() {
    return produces;
  }

  public void setProduces(List<FastMediaType> produces) {
    this.produces = produces;
  }

  public Set<FastMediaType> getConsumes() {
    return consumes;
  }

  public void setConsumes(Set<FastMediaType> consumes) {
    this.consumes = consumes;
  }

  public String getMethodPath() {
    return methodPath;
  }

  public void setMethodPath(String methodPath) {
    this.methodPath = methodPath;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public void setClazz(Class<?> clazz) {
    this.clazz = clazz;
  }

  public Class<?> getCurrentClazz() {
    return currentClazz;
  }

  public void setCurrentClazz(Class<?> currentClazz) {
    this.currentClazz = currentClazz;
  }

  public void setRootPath(String rootPath) {
    this.rootPath = rootPath;
  }

  public String getRootPath() {
    return rootPath;
  }

  public void setRouter(Router<ResourceArray> router) {
    this.router = router;
  }

  public Router<ResourceArray> getRouter() {
    return router;
  }

  public Set<String> getMethodsWithResources() {
    return methodsWithResources;
  }

  public void setMethodsWithResources(Set<String> methodsWithResources) {
    this.methodsWithResources = methodsWithResources;
  }

  public Environment getEnv() {
    return env;
  }

  public void setEnv(Environment env) {
    this.env = env;
  }

  public Object getSingleton() {
    return singleton;
  }

  public void setSingleton(Object singleton) {
    this.singleton = singleton;
  }

  public List<AbstractResource> getResources() {
    return resources;
  }

  public Set<String> getHttpMethods() {
    return httpMethods;
  }

  public void setHttpMethods(Set<String> httpMethods) {
    this.httpMethods = httpMethods;
  }

}
