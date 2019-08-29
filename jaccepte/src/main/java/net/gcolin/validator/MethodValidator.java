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
import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;

/**
 * A validator for a method.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MethodValidator<T> {

  private Consumer<ConstraintValidatorContextImpl<T>>[] validators;
  private ValidatorFactory validatorFactory;

  /**
   * Create a MethodValidator.
   * 
   * @param groups the groups
   * @param descriptorsSet the descriptors
   * @param validatorFactory the factory
   * @param resolver the property resolver
   * @param cascadeParameterDescriptors the cascade descriptors
   * @param cascadeReturnDescriptors the return descriptors
   * @param paramNames the name of the parameters
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public MethodValidator(Class<?>[] groups, Set<ConstraintDescriptor<?>> descriptorsSet,
      ValidatorFactory validatorFactory, TraversableResolver resolver,
      Object[] cascadeParameterDescriptors, ReturnValueDescriptor cascadeReturnDescriptors,
      List<String> paramNames) {
    ValidatorImpl validator = (ValidatorImpl) validatorFactory.getValidator();
    this.validatorFactory = validatorFactory;
    validators = new Consumer[descriptorsSet.size() + cascadeParameterDescriptors.length
        + (cascadeReturnDescriptors == null ? 0 : 1)];
    Iterator<ConstraintDescriptor<?>> it = descriptorsSet.iterator();
    addValidators(paramNames, it);
    for (int j = 0, i = descriptorsSet.size(); j < cascadeParameterDescriptors.length; i++, j++) {
      ParameterDescriptorImpl paramDescriptor =
          (ParameterDescriptorImpl) cascadeParameterDescriptors[j];
      Node node = new NodeImpl(paramDescriptor.getName(), ElementKind.PARAMETER);
      Class<?>[] convertedGroups =
          Util.convertGroups(groups, paramDescriptor.getGroupConversions());
      BeanValidator<?> bd =
          validator.getBeanValidator(paramDescriptor.getElementClass(), convertedGroups);
      validators[i] = new ParameterCascadeValidatorConsumer(resolver, node, paramDescriptor, bd);
    }
    if (cascadeReturnDescriptors != null) {
      Node node = new NodeImpl("<return value>", ElementKind.RETURN_VALUE);
      Class<?>[] convertedGroups =
          Util.convertGroups(groups, cascadeReturnDescriptors.getGroupConversions());
      BeanValidator<?> bd =
          validator.getBeanValidator(cascadeReturnDescriptors.getElementClass(), convertedGroups);
      validators[validators.length - 1] = new ReturnCascadeValidatorConsumer(resolver, node, bd);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void addValidators(List<String> paramNames, Iterator<ConstraintDescriptor<?>> it) {
    int idx = 0;
    while (it.hasNext()) {
      final ConstraintDescriptorImpl<Annotation> desc =
          (ConstraintDescriptorImpl<Annotation>) it.next();
      if (desc.getType() == ElementType.PARAMETER) {
        validators[idx] = new ParameterValidatorConsumer(desc, paramNames);
      } else if (isExecutable(desc)) {
        validators[idx] = new ExecutableValidatorConsumer(desc);
      }
      idx++;
    }
  }

  private boolean isExecutable(final ConstraintDescriptorImpl<Annotation> desc) {
    return desc.getType() == ElementType.METHOD || desc.getType() == ElementType.CONSTRUCTOR;
  }

  private static class ReturnCascadeValidatorConsumer<T>
      implements Consumer<ConstraintValidatorContextImpl<T>> {

    private TraversableResolver resolver;
    private Node node;
    private BeanValidator<?> bd;

    public ReturnCascadeValidatorConsumer(TraversableResolver resolver, Node node,
        BeanValidator<?> bd) {
      this.resolver = resolver;
      this.node = node;
      this.bd = bd;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void accept(ConstraintValidatorContextImpl<T> context) {
      if (resolver.isCascadable(context.getO(), node, context.getBeanClass(), context.getPath(),
          ElementType.METHOD)) {
        Object leaf = context.getExecutableReturnValue();
        if (leaf != null) {
          context.getPath().add(node);
          Object leafBean = context.getLeafBean();
          context.setLeafBean(leaf);

          Consumer<ConstraintValidatorContextImpl<?>>[] bdvalidators =
              (Consumer[]) bd.getValidators();
          for (int m = 0, k = bd.getValidators().length; m < k; m++) {
            bdvalidators[m].accept((ConstraintValidatorContextImpl) context);
          }
          context.setLeafBean(leafBean);
          context.getPath().remove(node);
        }
      }
    }

  }

  private static class ParameterCascadeValidatorConsumer<T>
      implements Consumer<ConstraintValidatorContextImpl<T>> {

    private TraversableResolver resolver;
    private Node node;
    private ParameterDescriptorImpl propertyDesciptor;
    private BeanValidator<?> bd;

    public ParameterCascadeValidatorConsumer(TraversableResolver resolver, Node node,
        ParameterDescriptorImpl propertyDesciptor, BeanValidator<?> bd) {
      this.resolver = resolver;
      this.node = node;
      this.propertyDesciptor = propertyDesciptor;
      this.bd = bd;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void accept(ConstraintValidatorContextImpl<T> context) {
      if (resolver.isCascadable(context.getO(), node, context.getBeanClass(), context.getPath(),
          ElementType.PARAMETER)) {
        Object leaf = context.getExecutableParameters()[propertyDesciptor.getIndex()];
        if (leaf != null) {
          Node leafNode = new NodeImpl(node.getName(), ElementKind.BEAN);
          context.getPath().add(leafNode);
          Object leafBean = context.getLeafBean();
          context.setLeafBean(leaf);

          Consumer<ConstraintValidatorContextImpl<?>>[] bdvalidators =
              (Consumer[]) bd.getValidators();
          for (int m = 0, k = bd.getValidators().length; m < k; m++) {
            bdvalidators[m].accept((ConstraintValidatorContextImpl) context);
          }
          context.setLeafBean(leafBean);
          context.getPath().remove(leafNode);
        }
      }
    }

  }

  private static class ParameterValidatorConsumer<T>
      implements Consumer<ConstraintValidatorContextImpl<T>> {

    private ConstraintDescriptorImpl<Annotation> desc;
    private List<String> paramNames;

    public ParameterValidatorConsumer(ConstraintDescriptorImpl<Annotation> desc,
        List<String> paramNames) {
      this.desc = desc;
      this.paramNames = paramNames;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void accept(ConstraintValidatorContextImpl<T> context) {
      context.setDescriptor(desc);
      int index = (Integer) desc.getAttributes().get(BeanDescriptorImpl.PARAM_INDEX);
      Object paramT = context.getExecutableParameters()[index];

      List<ConstraintValidator> list = (List) desc.getConstraintValidators();

      context.setValue(paramT);
      for (int j = 0, l = list.size(); j < l; j++) {
        if (!list.get(j).isValid(paramT, context)) {
          String name = paramNames.get(index);

          ConstraintViolationBuilder cb =
              context.buildConstraintViolationWithTemplate(desc.getMessageTemplate());
          if (!name.equals("arg" + index)) {
            cb.addPropertyNode(name).addConstraintViolation();
          } else {
            cb.addParameterNode(index).addConstraintViolation();
          }
        }
      }
      context.setValue(null);
    }

  }

  private static class ExecutableValidatorConsumer<T>
      implements Consumer<ConstraintValidatorContextImpl<T>> {

    private ConstraintDescriptorImpl<Annotation> desc;

    public ExecutableValidatorConsumer(ConstraintDescriptorImpl<Annotation> desc) {
      this.desc = desc;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void accept(ConstraintValidatorContextImpl<T> context) {
      context.setDescriptor(desc);

      List<ConstraintValidator> list = (List) desc.getConstraintValidators();

      for (int j = 0, l = list.size(); j < l; j++) {
        if (!list.get(j).isValid(context.getExecutableReturnValue(), context)) {
          context.buildConstraintViolationWithTemplate(desc.getMessageTemplate())
              .addConstraintViolation();
        }
      }
    }

  }


  /**
   * Validate a method.
   * 
   * @param obj the bean
   * @param beanClass the bean type
   * @param executable the method
   * @param args the arguments
   * @param value the return value
   * @return the violations or an empty set
   */
  public Set<ConstraintViolation<T>> validate(T obj, Class<T> beanClass, Executable executable,
      Object[] args, Object value) {
    ConstraintValidatorContextImpl<T> context = new ConstraintValidatorContextImpl<>();
    context.setValidatorFactory(validatorFactory);
    context.setO(obj);
    context.setBeanClass(beanClass);
    context.getPath()
        .add(new NodeImpl(
            executable.getClass() == Method.class ? executable.getName()
                : executable.getDeclaringClass().getSimpleName(),
            executable instanceof Method ? ElementKind.METHOD : ElementKind.CONSTRUCTOR));
    context.setExecutableReturnValue(value);
    context.setExecutableParameters(args);

    for (int i = 0, l = validators.length; i < l; i++) {
      validators[i].accept(context);
    }
    return context.getViolations();
  }

}
