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

import net.gcolin.di.core.AbstractEnvironment;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.ws.rs.NameBinding;

/**
 * A small bean manager for interceptors and filters.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class BindingEnvironment<T> extends AbstractEnvironment<T> {

  public BindingEnvironment() {}

  private BindingEnvironment(Map<String, T> binding) {
    super(binding);
  }

  @Override
  public boolean isQualifier(Class<? extends Annotation> annotationType) {
    return annotationType.isAnnotationPresent(NameBinding.class);
  }

  public BindingEnvironment<T> newInstance() {
    return new BindingEnvironment<>(getBinding());
  }
}
