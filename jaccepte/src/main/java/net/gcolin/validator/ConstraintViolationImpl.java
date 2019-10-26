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

import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.MessageInterpolator.Context;
import javax.validation.Path;
import javax.validation.metadata.ConstraintDescriptor;

/**
 * A ConstraintViolation implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ConstraintViolationImpl<T> implements ConstraintViolation<T>, Context {

  private String messageTemplate;
  private T rootBean;
  private Class<T> rootBeanClass;
  private Object leafBean;
  private Object[] executableParameters;
  private Object executableReturnValue;
  private Path path;
  private Object invalidValue;
  private ConstraintDescriptor<?> constraintDescriptor;
  private MessageInterpolator messageInterpolator;

  public void setMessageTemplate(String messageTemplate) {
    this.messageTemplate = messageTemplate;
  }

  public void setRootBean(T rootBean) {
    this.rootBean = rootBean;
  }

  public void setRootBeanClass(Class<T> rootBeanClass) {
    this.rootBeanClass = rootBeanClass;
  }

  public void setLeafBean(Object leafBean) {
    this.leafBean = leafBean;
  }

  public void setExecutableParameters(Object[] executableParameters) {
    this.executableParameters = executableParameters;
  }

  public void setExecutableReturnValue(Object executableReturnValue) {
    this.executableReturnValue = executableReturnValue;
  }

  public void setPath(Path path) {
    this.path = path;
  }

  public void setInvalidValue(Object invalidValue) {
    this.invalidValue = invalidValue;
  }

  public void setConstraintDescriptor(ConstraintDescriptor<?> constraintDescriptor) {
    this.constraintDescriptor = constraintDescriptor;
  }

  public void setMessageInterpolator(MessageInterpolator messageInterpolator) {
    this.messageInterpolator = messageInterpolator;
  }

  @Override
  public String getMessage() {
    return messageInterpolator.interpolate(messageTemplate, this);
  }

  @Override
  public String getMessageTemplate() {
    return messageTemplate;
  }

  @Override
  public T getRootBean() {
    return rootBean;
  }

  @Override
  public Class<T> getRootBeanClass() {
    return rootBeanClass;
  }

  @Override
  public Object getLeafBean() {
    return leafBean;
  }

  @Override
  public Object[] getExecutableParameters() {
    return executableParameters;
  }

  @Override
  public Object getExecutableReturnValue() {
    return executableReturnValue;
  }

  @Override
  public Path getPropertyPath() {
    return path;
  }

  @Override
  public Object getInvalidValue() {
    return invalidValue;
  }

  @Override
  public ConstraintDescriptor<?> getConstraintDescriptor() {
    return constraintDescriptor;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <U> U unwrap(Class<U> type) {
    if (type.isAssignableFrom(this.getClass())) {
      return (U) this;
    }
    throw new IllegalArgumentException("cannot wrap to " + type);
  }

  @Override
  public Object getValidatedValue() {
    return getInvalidValue();
  }

  @Override
  public String toString() {
    return getPropertyPath() + " " + getMessage();
  }

}
