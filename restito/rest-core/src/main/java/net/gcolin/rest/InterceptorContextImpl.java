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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.ws.rs.ext.InterceptorContext;

/**
 * A base InterceptorContext implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class InterceptorContextImpl implements InterceptorContext {

  private InvocationContext context;
  private Annotation[] annotations;
  private Class<?> type;
  private Type genericType;

  /**
   * Create an InterceptorContextImpl.
   * 
   * @param context the context of the request
   * @param annotations the annotations of the resource
   * @param type the entity type
   * @param genericType the entity generic typetype
   */
  public InterceptorContextImpl(InvocationContext context, Annotation[] annotations, Class<?> type,
      Type genericType) {
    super();
    this.context = context;
    this.annotations = annotations;
    this.type = type;
    this.genericType = genericType;
  }

  @Override
  public Object getProperty(String name) {
    return context.getProperty(name);
  }

  @Override
  public Collection<String> getPropertyNames() {
    return context.getPropertyNames();
  }

  @Override
  public void setProperty(String name, Object object) {
    context.setProperty(name, object);
  }

  @Override
  public void removeProperty(String name) {
    context.removeProperty(name);
  }

  @Override
  public Annotation[] getAnnotations() {
    return annotations;
  }

  @Override
  public void setAnnotations(Annotation[] annotations) {
    this.annotations = annotations;
  }

  @Override
  public Class<?> getType() {
    return type;
  }

  @Override
  public void setType(Class<?> type) {
    this.type = type;
  }

  @Override
  public Type getGenericType() {
    return genericType;
  }

  @Override
  public void setGenericType(Type genericType) {
    this.genericType = genericType;
  }

}
