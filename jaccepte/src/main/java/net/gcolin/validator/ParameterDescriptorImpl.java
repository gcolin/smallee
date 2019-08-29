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

package net.gcolin.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;

import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.validation.metadata.ParameterDescriptor;

/**
 * A ParameterDescriptor implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ParameterDescriptorImpl extends AbstractElementCascadableDescriptor
    implements
      ParameterDescriptor {

  private int index;
  private String name;

  
  /**
   * Create a ParameterDescriptorImpl.
   * 
   * @param elementClass the type of the parameter
   * @param index the index of the parameter
   * @param name the name of the parameter
   */
  public ParameterDescriptorImpl(Class<?> elementClass, int index, String name) {
    super(elementClass);
    setContraints(new HashSet<>());
    this.index = index;
    this.name = name;
  }

  @Override
  public int getIndex() {
    return index;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  void update(Method method) {
    for (Annotation annotation : method.getParameterAnnotations()[index]) {
      if (annotation.annotationType() == ConvertGroup.class) {
        getGroupConversions().add(new GroupConversionDescriptorImpl((ConvertGroup) annotation));
      } else if (annotation.annotationType() == Valid.class) {
        cascaded = true;
      }
    }
  }

  void update(Constructor<?> constructor) {
    for (Annotation annotation : constructor.getParameterAnnotations()[index]) {
      if (annotation.annotationType() == ConvertGroup.class) {
        getGroupConversions().add(new GroupConversionDescriptorImpl((ConvertGroup) annotation));
      } else if (annotation.annotationType() == Valid.class) {
        cascaded = true;
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [" + getName() + "->" + getConstraintDescriptors().size()
        + (cascaded ? "+cascade" : "") + "]";
  }
}
