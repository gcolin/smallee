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

import net.gcolin.common.reflect.Reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;

/**
 * A validator on a bean or a property.
 * 
 * <p>
 * Thread safe
 * </p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
class BeanValidator<T> implements Function<T, Set<ConstraintViolation<T>>> {

  private Consumer<ConstraintValidatorContextImpl<T>>[] validators;
  private ValidatorFactory validatorFactory;

  @SuppressWarnings({"unchecked"})
  public BeanValidator(Class<?>[] groups, Set<ConstraintDescriptor<?>> descriptorsSet,
      ValidatorFactory validatorFactory, TraversableResolver resolver,
      Object[] cascadePropertyDescriptors, ValidatorImpl validator) {
    this.validatorFactory = validatorFactory;
    validators = new Consumer[descriptorsSet.size() + cascadePropertyDescriptors.length];
    Iterator<ConstraintDescriptor<?>> it = descriptorsSet.iterator();
    addPropertyValidators(resolver, it);
    for (int i = descriptorsSet.size(), j = 0; i < validators.length; i++, j++) {
      PropertyDescriptorImpl propertyDescriptor =
          (PropertyDescriptorImpl) cascadePropertyDescriptors[j];
      Member validMember = propertyDescriptor.getMember();
      Node node = new NodeImpl(propertyDescriptor.getPropertyName(), ElementKind.PROPERTY);
      Class<?>[] convertedGroups =
          Util.convertGroups(groups, propertyDescriptor.getGroupConversions());
      if (validMember instanceof Field) {
        Field field = (Field) validMember;
        Reflect.enable(field);
        BeanValidator<?> bd = validator.getBeanValidator(field.getType(), convertedGroups);
        validators[i] = new ConsumerCascadeValidator<>(resolver, node, context -> {
          try {
            return field.get(context.getLeafBean());
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new ValidationException(ex);
          }
        }, bd, ElementType.FIELD);
      } else if (validMember instanceof Method) {
        Method met = (Method) validMember;
        Reflect.enable(met);
        BeanValidator<?> bd = validator.getBeanValidator(met.getReturnType(), convertedGroups);
        validators[i] = new ConsumerCascadeValidator<>(resolver, node, context -> {
          try {
            return met.invoke(context.getLeafBean());
          } catch (IllegalArgumentException | IllegalAccessException
              | InvocationTargetException ex) {
            throw new ValidationException(ex);
          }
        }, bd, ElementType.METHOD);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void addPropertyValidators(TraversableResolver resolver,
      Iterator<ConstraintDescriptor<?>> it) {
    int index = 0;
    while (it.hasNext()) {
      final ConstraintDescriptorImpl<Annotation> desc =
          (ConstraintDescriptorImpl<Annotation>) it.next();
      Member member = desc.getMember();
      if (member == null) {
        Node node = new NodeImpl(desc.getDeclaringClass().getSimpleName(), ElementKind.BEAN);
        validators[index] = new ConsumerValidator<>(resolver, node, desc,
            context -> context.getLeafBean(), desc.getType());
      } else if (member instanceof Field) {
        Field field = (Field) member;
        Reflect.enable(field);
        Node node = new NodeImpl(field.getName(), ElementKind.PROPERTY);
        validators[index] = new ConsumerValidator<>(resolver, node, desc, context -> {
          try {
            return field.get(context.getLeafBean());
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new ValidationException(ex);
          }
        }, desc.getType());
      } else {
        Method me = (Method) member;
        Node node = new NodeImpl(Reflect.getPropertyName(me), ElementKind.PROPERTY);
        Reflect.enable(me);
        validators[index] = new ConsumerValidator<>(resolver, node, desc, context -> {
          try {
            return me.invoke(context.getLeafBean());
          } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new ValidationException(ex);
          }
        }, desc.getType());
      }
      index++;
    }
  }

  public Consumer<ConstraintValidatorContextImpl<T>>[] getValidators() {
    return validators;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Set<ConstraintViolation<T>> apply(T obj) {
    ConstraintValidatorContextImpl<T> context = new ConstraintValidatorContextImpl<>();
    context.setValidatorFactory(validatorFactory);
    context.setO(obj);
    context.setLeafBean(obj);
    context.setBeanClass((Class<T>) obj.getClass());
    for (int i = 0, l = validators.length; i < l; i++) {
      validators[i].accept(context);
    }
    return context.getViolations();
  }

  Set<ConstraintViolation<T>> applyWithValue(Object value, Class<T> clazz) {
    ConstraintValidatorContextImpl<T> context = new ConstraintValidatorContextImpl<>();
    context.setValidatorFactory(validatorFactory);
    context.setValue(value);
    context.setBeanClass(clazz);
    for (int i = 0, l = validators.length; i < l; i++) {
      validators[i].accept(context);
    }
    return context.getViolations();
  }

  private static class ConsumerCascadeValidator<T>
      implements Consumer<ConstraintValidatorContextImpl<T>> {

    private TraversableResolver resolver;
    private Node node;
    private Function<ConstraintValidatorContextImpl<T>, Object> getter;
    private BeanValidator<?> bd;
    private ElementType elementType;

    public ConsumerCascadeValidator(TraversableResolver resolver, Node node,
        Function<ConstraintValidatorContextImpl<T>, Object> getter, BeanValidator<?> bd,
        ElementType elementType) {
      this.resolver = resolver;
      this.node = node;
      this.getter = getter;
      this.bd = bd;
      this.elementType = elementType;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void accept(ConstraintValidatorContextImpl<T> context) {
      if (context.getViolations().isEmpty()
          && resolver.isReachable(context.getO(), node, context.getBeanClass(), context.getPath(),
              elementType)
          && resolver.isCascadable(context.getO(), node, context.getBeanClass(), context.getPath(),
              elementType)) {
        Object leaf = getter.apply(context);
        if (leaf != null) {
          Node leafNode = new NodeImpl(node.getName(), ElementKind.BEAN);
          context.getPath().add(leafNode);
          Object leafBean = context.getLeafBean();
          context.setLeafBean(leaf);
          for (int m = 0, k = bd.validators.length; m < k; m++) {
            bd.validators[m].accept((ConstraintValidatorContextImpl) context);
          }
          context.setLeafBean(leafBean);
          context.getPath().remove(leafNode);
        }
      }
    }
  }

  private static class ConsumerValidator<T> implements Consumer<ConstraintValidatorContextImpl<T>> {

    private TraversableResolver resolver;
    private Node node;
    private ConstraintDescriptorImpl<Annotation> desc;
    private Function<ConstraintValidatorContextImpl<T>, Object> getter;
    private ElementType elementType;

    public ConsumerValidator(TraversableResolver resolver, Node node,
        ConstraintDescriptorImpl<Annotation> desc,
        Function<ConstraintValidatorContextImpl<T>, Object> getter, ElementType elementType) {
      this.resolver = resolver;
      this.node = node;
      this.desc = desc;
      this.getter = getter;
      this.elementType = elementType;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void accept(ConstraintValidatorContextImpl<T> context) {
      if (resolver.isReachable(context.getO(), node, context.getBeanClass(), context.getPath(),
          elementType)) {

        context.setDescriptor(desc);
        Object paramT = context.getValue();

        if (paramT == null) {
          paramT = getter.apply(context);
        }
        List<ConstraintValidator> list = (List) desc.getConstraintValidators();

        context.setValue(paramT);
        for (int j = 0, l = list.size(); j < l; j++) {
          if (!list.get(j).isValid(paramT, context)) {
            context.buildConstraintViolationWithTemplate(desc.getMessageTemplate())
                .addPropertyNode(node.getName()).addConstraintViolation();
          }
        }
        context.setValue(null);
      }
    }

  }
}
