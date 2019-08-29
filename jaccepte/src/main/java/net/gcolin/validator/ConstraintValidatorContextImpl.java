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
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeBuilderCustomizableContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeBuilderDefinedContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.LeafNodeContextBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeContextBuilder;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;

/**
 * A ConstraintValidatorContext implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ConstraintValidatorContextImpl<T> implements ConstraintValidatorContext {

  private ValidatorFactory validatorFactory;
  private ConstraintDescriptor<Annotation> descriptor;
  private PathImpl path = new PathImpl();
  private Set<ConstraintViolation<T>> violations = new HashSet<>();
  private T obj;
  private Class<T> beanClass;
  private Object[] executableParameters;
  private Object executableReturnValue;
  private Object value;
  private Object leafBean;
  private boolean disableDefaultConstraintViolation;

  public Object[] getExecutableParameters() {
    return executableParameters;
  }

  public Object getExecutableReturnValue() {
    return executableReturnValue;
  }

  public Object getLeafBean() {
    return leafBean;
  }

  public void setBeanClass(Class<T> beanClass) {
    this.beanClass = beanClass;
  }

  public Class<T> getBeanClass() {
    return beanClass;
  }

  public PathImpl getPath() {
    return path;
  }

  public Object getValue() {
    return value;
  }

  public Set<ConstraintViolation<T>> getViolations() {
    return violations;
  }

  public void setValidatorFactory(ValidatorFactory validatorFactory) {
    this.validatorFactory = validatorFactory;
  }

  public void setDescriptor(ConstraintDescriptor<Annotation> descriptor) {
    this.descriptor = descriptor;
  }

  public void setPath(PathImpl path) {
    this.path = path;
  }

  public void setO(T obj) {
    this.obj = obj;
  }

  public T getO() {
    return obj;
  }

  public void setExecutableParameters(Object[] executableParameters) {
    this.executableParameters = executableParameters;
  }

  public void setExecutableReturnValue(Object executableReturnValue) {
    this.executableReturnValue = executableReturnValue;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public void setLeafBean(Object leafBean) {
    this.leafBean = leafBean;
  }

  @Override
  public void disableDefaultConstraintViolation() {
    disableDefaultConstraintViolation = true;
  }

  public boolean isDisableDefaultConstraintViolation() {
    return disableDefaultConstraintViolation;
  }

  @Override
  public String getDefaultConstraintMessageTemplate() {
    return descriptor.getMessageTemplate();
  }

  @Override
  public ConstraintViolationBuilder buildConstraintViolationWithTemplate(String messageTemplate) {
    return new ConstraintViolationBuilderImpl((PathImpl) path.clone(), messageTemplate);
  }

  @Override
  public <X> X unwrap(Class<X> type) {
    if (type.isAssignableFrom(this.getClass())) {
      return type.cast(this);
    }
    throw new IllegalArgumentException("cannot wrap to " + type);
  }

  private ConstraintValidatorContext addConstraintViolation0(PathImpl path, String message) {
    ConstraintViolationImpl<T> cv = new ConstraintViolationImpl<>();
    cv.setConstraintDescriptor(descriptor);
    cv.setExecutableParameters(executableParameters);
    cv.setExecutableReturnValue(executableReturnValue);
    cv.setInvalidValue(value);
    cv.setMessageInterpolator(validatorFactory.getMessageInterpolator());
    cv.setPath(path);
    cv.setRootBean(obj);
    cv.setRootBeanClass(beanClass);
    cv.setLeafBean(leafBean);
    cv.setMessageTemplate(message);
    violations.add(cv);
    return this;
  }

  private class ConstraintViolationBuilderImpl implements ConstraintViolationBuilder {

    private String messageTemplate;
    private PathImpl currentPath;

    public ConstraintViolationBuilderImpl(PathImpl currentPath, String messageTemplate) {
      this.messageTemplate = messageTemplate;
      this.currentPath = currentPath;
    }

    @Override
    public NodeBuilderDefinedContext addNode(String name) {
      NodeImpl node = new NodeImpl(name, ElementKind.PROPERTY);
      currentPath.add(node);
      return new NodeBuilder(currentPath, messageTemplate, node);
    }

    @Override
    public NodeBuilderCustomizableContext addPropertyNode(String name) {
      NodeImpl node = new NodeImpl(name, ElementKind.PROPERTY);
      currentPath.add(node);
      return new NodeBuilder(currentPath, messageTemplate, node);
    }

    @Override
    public LeafNodeBuilderCustomizableContext addBeanNode() {
      NodeImpl node = new NodeImpl(null, ElementKind.BEAN);
      currentPath.add(node);
      return new LeafNodeBuilder(currentPath, messageTemplate, node);
    }

    @Override
    public NodeBuilderDefinedContext addParameterNode(int index) {
      NodeImpl node = new NodeImpl(index, ElementKind.PARAMETER);
      currentPath.add(node);
      return new NodeBuilder(currentPath, messageTemplate, node);
    }

    @Override
    public ConstraintValidatorContext addConstraintViolation() {
      return addConstraintViolation0(currentPath, messageTemplate);
    }

  }

  private class LeafNodeBuilder
      implements
        LeafNodeContextBuilder,
        LeafNodeBuilderCustomizableContext,
        LeafNodeBuilderDefinedContext {

    private PathImpl currentPath;
    private String message;
    private NodeImpl node;

    public LeafNodeBuilder(PathImpl currentPath, String message, NodeImpl node) {
      super();
      this.currentPath = currentPath;
      this.message = message;
      this.node = node;
    }

    @Override
    public ConstraintValidatorContext addConstraintViolation() {
      return addConstraintViolation0(currentPath, message);
    }

    @Override
    public LeafNodeContextBuilder inIterable() {
      node.setInIterable(true);
      return this;
    }

    @Override
    public LeafNodeBuilderDefinedContext atIndex(Integer index) {
      node.setIndex(index);
      return this;
    }

    @Override
    public LeafNodeBuilderDefinedContext atKey(Object key) {
      node.setKey(key);
      return this;
    }

  }

  private class NodeBuilder
      implements
        NodeContextBuilder,
        NodeBuilderDefinedContext,
        NodeBuilderCustomizableContext {

    private PathImpl currentPath;
    private String message;
    private NodeImpl node;

    public NodeBuilder(PathImpl currentPath, String message, NodeImpl node) {
      super();
      this.currentPath = currentPath;
      this.message = message;
      this.node = node;
    }

    @Override
    public NodeContextBuilder inIterable() {
      node.setInIterable(true);
      return this;
    }

    @Override
    public LeafNodeBuilderCustomizableContext addBeanNode() {
      NodeImpl node = new NodeImpl(null, ElementKind.BEAN);
      currentPath.add(node);
      return new LeafNodeBuilder(currentPath, message, node);
    }

    @Override
    public ConstraintValidatorContext addConstraintViolation() {
      return addConstraintViolation0(currentPath, message);
    }

    @Override
    public NodeBuilderCustomizableContext addNode(String name) {
      currentPath.add(new NodeImpl(name, ElementKind.PROPERTY));
      return this;
    }

    @Override
    public NodeBuilderCustomizableContext addPropertyNode(String name) {
      currentPath.add(new NodeImpl(name, ElementKind.PROPERTY));
      return this;
    }

    @Override
    public NodeBuilderDefinedContext atKey(Object key) {
      node.setKey(key);
      return this;
    }

    @Override
    public NodeBuilderDefinedContext atIndex(Integer index) {
      node.setIndex(index);
      return this;
    }

  }

}
