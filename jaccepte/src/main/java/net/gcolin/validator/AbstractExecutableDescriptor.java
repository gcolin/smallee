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
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.metadata.CrossParameterDescriptor;
import javax.validation.metadata.ExecutableDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

/**
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class AbstractExecutableDescriptor extends AbstractElementDescriptor
    implements
      ExecutableDescriptor {

  private List<ParameterDescriptor> parameterDescriptors = new ArrayList<>();
  private String name;
  private CrossParameterDescriptor crossParameterDescriptor = new CrossParameterDescriptorImpl();
  private ReturnValueDescriptorImpl returnValueDescriptor;
  private boolean hasValid;

  /**
   * Create an AbstractExecutableDescriptor.
   * 
   * @param elementClass the return type of the executable
   * @param name         the name of the executable
   */
  public AbstractExecutableDescriptor(Class<?> elementClass, String name) {
    super(elementClass);
    this.name = name;
    returnValueDescriptor = new ReturnValueDescriptorImpl(elementClass);
  }

  public void update(Method method) {
    update0(method);
  }

  public void update(Constructor<?> constructor) {
    update0(constructor);
  }

  private void update0(Executable executable) {
    if (executable.isAnnotationPresent(Valid.class)) {
      returnValueDescriptor.setCascaded();
      hasValid = true;
    }
    if (hasValid) {
      return;
    }
    for (Annotation[] array : executable.getParameterAnnotations()) {
      for (Annotation a : array) {
        if (a.annotationType() == Valid.class) {
          hasValid = true;
          break;
        }
      }
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<ParameterDescriptor> getParameterDescriptors() {
    return parameterDescriptors;
  }

  @Override
  public CrossParameterDescriptor getCrossParameterDescriptor() {
    return crossParameterDescriptor;
  }

  @Override
  public ReturnValueDescriptor getReturnValueDescriptor() {
    return returnValueDescriptor;
  }

  @Override
  public boolean hasConstrainedParameters() {
    return !parameterDescriptors.isEmpty() || crossParameterDescriptor.hasConstraints() || hasValid;
  }

  @Override
  public boolean hasConstrainedReturnValue() {
    return returnValueDescriptor.hasConstraints();
  }

  public void setParameterDescriptors(List<ParameterDescriptor> parameterDescriptors) {
    this.parameterDescriptors = parameterDescriptors;
  }

  public void setCrossParameterDescriptor(CrossParameterDescriptor crossParameterDescriptor) {
    this.crossParameterDescriptor = crossParameterDescriptor;
  }

  public void setHasValid(boolean hasValid) {
    this.hasValid = hasValid;
  }

}
