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

import java.io.Closeable;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.ElementDescriptor.ConstraintFinder;
import javax.validation.metadata.MethodDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gcolin.common.collection.Func;
import net.gcolin.common.jmx.Jmx;
import net.gcolin.common.reflect.Reflect;

/**
 * A Validator and ExecutableValidator implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ValidatorImpl implements Validator, ExecutableValidator, Closeable, ValidatorBean {

  private static final String WITH_GROUPS = " with groups ";
  public static final Logger LOG = LoggerFactory.getLogger("net.gcolin.validator");
  private static Class<?>[] DEFAULT_GROUP = {Default.class};
  private ValidatorConfigurationImpl configuration;
  private ValidatorFactoryImpl factory;
  private Map<String, BeanValidator<?>> cache = new ConcurrentHashMap<>();
  private Map<String, MethodValidator<?>> cacheExecutabe = new ConcurrentHashMap<>();
  private Map<Class<?>, BeanDescriptorImpl> descriptors = new ConcurrentHashMap<>();
  private static final AtomicInteger VALIDATOR_NUMBER = new AtomicInteger();
  private String name;

  /**
   * Create a Validator.
   * 
   * @param configuration a configuration
   * @param factory a factory
   */
  public ValidatorImpl(ValidatorConfigurationImpl configuration, ValidatorFactoryImpl factory) {
    this.factory = factory;
    this.configuration = configuration;
    startJmx();
    LOG.debug("create new Validator");
  }

  /**
   * Start JMX for this validator.
   */
  public void startJmx() {
    if (Reflect.exists("net.gcolin.jmx.Jmx", ValidatorImpl.class.getClassLoader())) {
      name =
          "net.gcolin.validator:type=Validator,name=instance" + VALIDATOR_NUMBER.incrementAndGet();
      Jmx.publish(name, this, ValidatorBean.class);
    }
  }

  @Override
  public void close() {
    for (BeanDescriptorImpl bd : descriptors.values()) {
      for (ConstraintDescriptor<?> c : bd.getConstraintDescriptors()) {
        for (ConstraintValidator<?, ?> cv : ((ConstraintDescriptorImpl<?>) c)
            .getConstraintValidators()) {
          configuration.getConstraintValidatorFactory().releaseInstance(cv);
        }
      }
    }
    cache.clear();
    descriptors.clear();
    cacheExecutabe.clear();
  }

  private Class<?>[] formatGroups(Class<?>[] array) {
    if (array.length == 0) {
      return DEFAULT_GROUP;
    } else {
      return array;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
    return getBeanValidator((Class<T>) object.getClass(), formatGroups(groups)).apply(object);
  }

  /**
   * Create a validator for a bean.
   * 
   * @param <T> type of bean
   * @param cl a bean class
   * @param groups groups
   * @return a bean validator
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T> BeanValidator<T> getBeanValidator(Class<T> cl, Class<?>[] groups) {
    String key = createKey(cl, groups).toString();
    BeanValidator<T> item = (BeanValidator) cache.get(key);
    if (item == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("create BeanValidator for " + cl + WITH_GROUPS + Arrays.asList(groups));
      }
      BeanDescriptorImpl bd = (BeanDescriptorImpl) getConstraintsForClass(cl);
      ConstraintFinder finder = bd.findConstraints();
      if (groups.length > 0) {
        finder = finder.unorderedAndMatchingGroups(groups);
      }
      item = new BeanValidator<T>(groups,
          finder.getConstraintDescriptors().stream().filter(x -> isFieldOrGetterOrBean(x))
              .collect(Collectors.toSet()),
          factory, configuration.getTraversableResolver(),
          bd.getConstrainedProperties().stream().filter(x -> x.isCascaded()).toArray(), this);
      cache.put(key, item);
    }
    return item;
  }

  /**
   * Create a validator for a property.
   * 
   * @param beanType a bean class
   * @param propertyName name of the property
   * @param groups groups
   * @return a bean validator for a property
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private <T> BeanValidator<T> getBeanValidator(Class<T> beanType, String propertyName,
      Class<?>... groups) {
    String key = createKey(beanType, groups).append('@').append(propertyName).toString();
    BeanValidator<T> item = (BeanValidator) cache.get(key);
    if (item == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("create BeanValidator for " + beanType + " and property " + propertyName
            + WITH_GROUPS + Arrays.asList(groups));
      }
      BeanDescriptorImpl bd = (BeanDescriptorImpl) getConstraintsForClass(beanType);
      ConstraintFinder finder = bd.getConstraintsForProperty(propertyName).findConstraints();
      if (groups.length > 0) {
        finder = finder.unorderedAndMatchingGroups(groups);
      }
      item = new BeanValidator<T>(groups, finder.getConstraintDescriptors(), factory,
          configuration.getTraversableResolver(),
          bd.getConstrainedProperties().stream()
              .filter(x -> x.isCascaded() && x.getPropertyName().equals(propertyName)).toArray(),
          this);
      cache.put(key, (BeanValidator) item);
    }
    return item;
  }

  private boolean isFieldOrGetterOrBean(ConstraintDescriptor<?> descriptor) {
    ConstraintDescriptorImpl<?> cd = (ConstraintDescriptorImpl<?>) descriptor;
    Member met = cd.getMember();
    return met == null || met instanceof Field
        || met instanceof Method && Reflect.isGetter((Method) met);
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName,
      Class<?>... groups) {
    BeanValidator<T> item =
        getBeanValidator((Class<T>) object.getClass(), propertyName, formatGroups(groups));
    return item.apply(object);
  }

  @Override
  public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName,
      Object value, Class<?>... groups) {
    BeanValidator<T> item = getBeanValidator(beanType, propertyName, formatGroups(groups));
    return item.applyWithValue(value, beanType);
  }

  @Override
  public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
    BeanDescriptorImpl bd = descriptors.get(clazz);
    if (bd == null) {
      bd = new BeanDescriptorImpl(clazz, configuration);
      descriptors.put(clazz, bd);
    }
    return bd;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T unwrap(Class<T> type) {
    if (type.isAssignableFrom(this.getClass())) {
      return (T) this;
    }
    throw new IllegalArgumentException("cannot wrap to " + type);
  }

  @Override
  public ExecutableValidator forExecutables() {
    return this;
  }

  private StringBuilder createKey(Class<?> cl, Class<?>... groups) {
    StringBuilder keyBuilder = new StringBuilder(cl.getName());
    for (Class<?> group : groups) {
      keyBuilder.append(",").append(group.getName());
    }
    if (groups.length == 0) {
      keyBuilder.append(",").append(Default.class.getName());
    }
    return keyBuilder;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> Set<ConstraintViolation<T>> validateParameters(T bean, Method method, Object[] args,
      Class<?>... gr) {
    Class<?>[] groups = formatGroups(gr);
    String key = createKey(bean.getClass(), groups).append("@p@").append(method).toString();
    MethodValidator<T> item = (MethodValidator) cacheExecutabe.get(key);
    if (item == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("create MethodValidator for parameters on " + method + WITH_GROUPS
            + Arrays.asList(groups));
      }
      BeanDescriptor bd = getConstraintsForClass(bean.getClass());
      MethodDescriptor md =
          bd.getConstraintsForMethod(method.getName(), method.getParameterTypes());
      ConstraintFinder finder = md.findConstraints().declaredOn(ElementType.PARAMETER);
      if (groups.length > 0) {
        finder = finder.unorderedAndMatchingGroups(groups);
      }

      item = new MethodValidator<T>(groups, finder.getConstraintDescriptors(), factory,
          configuration.getTraversableResolver(),
          md.getParameterDescriptors().stream().filter(x -> x.isCascaded()).toArray(), null,
          configuration.getParameterNameProvider().getParameterNames(method));
      cacheExecutabe.put(key, item);
    }
    return item.validate(bean, (Class<T>) bean.getClass(), method, args, null);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> Set<ConstraintViolation<T>> validateReturnValue(T bean, Method method, Object rv,
      Class<?>... gr) {
    Class<?>[] groups = formatGroups(gr);
    String key = createKey(bean.getClass(), groups).append("@r@").append(method).toString();
    MethodValidator<T> item = (MethodValidator) cacheExecutabe.get(key);
    if (item == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("create MethodValidator for return value on " + method + WITH_GROUPS
            + Arrays.asList(groups));
      }
      BeanDescriptor bd = getConstraintsForClass(bean.getClass());
      MethodDescriptor md =
          bd.getConstraintsForMethod(method.getName(), method.getParameterTypes());
      ConstraintFinder finder = md.findConstraints().declaredOn(ElementType.METHOD);

      if (groups.length > 0) {
        finder = finder.unorderedAndMatchingGroups(groups);
      }

      item = new MethodValidator<T>(groups, finder.getConstraintDescriptors(), factory,
          configuration.getTraversableResolver(), new Object[0],
          md.getReturnValueDescriptor().isCascaded() ? md.getReturnValueDescriptor() : null, null);
      cacheExecutabe.put(key, item);
    }
    return item.validate(bean, (Class<T>) bean.getClass(), method, null, rv);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> Set<ConstraintViolation<T>> validateConstructorParameters(
      Constructor<? extends T> constructor, Object[] args, Class<?>... gr) {
    Class<?>[] groups = formatGroups(gr);
    Class<T> beanClass = (Class<T>) constructor.getDeclaringClass();
    String key = createKey(beanClass, groups).append("@p@").append(constructor).toString();
    MethodValidator<T> item = (MethodValidator) cacheExecutabe.get(key);
    if (item == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("create MethodValidator for parameters on " + constructor + WITH_GROUPS
            + Arrays.asList(groups));
      }
      BeanDescriptor bd = getConstraintsForClass(beanClass);
      ConstructorDescriptor cd = bd.getConstraintsForConstructor(constructor.getParameterTypes());
      ConstraintFinder finder = cd.findConstraints().declaredOn(ElementType.PARAMETER);

      if (groups.length > 0) {
        finder = finder.unorderedAndMatchingGroups(groups);
      }

      item = new MethodValidator<T>(groups, finder.getConstraintDescriptors(), factory,
          configuration.getTraversableResolver(),
          cd.getParameterDescriptors().stream().filter(x -> x.isCascaded()).toArray(), null,
          configuration.getParameterNameProvider().getParameterNames(constructor));
      cacheExecutabe.put(key, item);
    }
    return item.validate(null, beanClass, constructor, args, null);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public <T> Set<ConstraintViolation<T>> validateConstructorReturnValue(
      Constructor<? extends T> constructor, T value, Class<?>... gr) {
    Class<?>[] groups = formatGroups(gr);
    Class<T> beanClass = (Class<T>) constructor.getDeclaringClass();
    String key = createKey(beanClass, groups).append("@r@").append(constructor).toString();
    MethodValidator<T> item = (MethodValidator) cacheExecutabe.get(key);
    if (item == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("create MethodValidator for return value on " + constructor + WITH_GROUPS
            + Arrays.asList(groups));
      }
      BeanDescriptor bd = getConstraintsForClass(beanClass);
      ConstructorDescriptor cd = bd.getConstraintsForConstructor(constructor.getParameterTypes());
      ConstraintFinder finder = cd.findConstraints().declaredOn(ElementType.CONSTRUCTOR);

      if (groups.length > 0) {
        finder = finder.unorderedAndMatchingGroups(groups);
      }

      item = new MethodValidator<T>(groups, finder.getConstraintDescriptors(), factory,
          configuration.getTraversableResolver(), new Object[0],
          cd.getReturnValueDescriptor().isCascaded() ? cd.getReturnValueDescriptor() : null, null);
      cacheExecutabe.put(key, item);
    }
    return item.validate(null, beanClass, constructor, null, value);
  }

  @Override
  public int getBeanDescriptorNumber() {
    return descriptors.size();
  }

  @Override
  public int getBeanValidatorNumber() {
    return cache.size();
  }

  @Override
  public int getMethodValidatorNumber() {
    return cacheExecutabe.size();
  }

  @Override
  public String getConstraintValidatorFactoryClassName() {
    return configuration.getConstraintValidatorFactoryClassName();
  }

  @Override
  public String getMessageInterpolatorClassName() {
    return configuration.getMessageInterpolatorClassName();
  }

  @Override
  public String getTraversableResolverClassName() {
    return configuration.getTraversableResolverClassName();
  }

  @Override
  public String getParameterNameProviderClassName() {
    return configuration.getParameterNameProviderClassName();
  }

  @Override
  public Map<String, String> getProperties() {
    return configuration.getProperties();
  }

  /**
   * Unregister this validator from JMX.
   */
  public void stopJmx() {
    if (name != null) {
      try {
        Jmx.unpublish(name);
      } finally {
        name = null;
      }
    }
  }

  @Override
  public Collection<String> getBeanDescriptors() {
    return Func.map(descriptors.keySet(), x -> x.getName());
  }

}
