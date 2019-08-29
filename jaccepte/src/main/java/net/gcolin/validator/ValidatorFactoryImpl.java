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

import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;

/**
 * A ValidatorFactory implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ValidatorFactoryImpl implements ValidatorFactory, ValidatorContext {

  private ValidatorConfigurationImpl config;
  private ValidatorImpl validator;
  private AtomicInteger nb = new AtomicInteger(0);

  /**
   * Create a ValidatorFactoryImpl.
   * @param config Configuration
   */
  public ValidatorFactoryImpl(ValidatorConfigurationImpl config) {
    super();
    this.config = config;
    validator = new ValidatorImpl(config, this);
  }

  void incr() {
    if (nb.getAndIncrement() == 0) {
      validator.startJmx();
    }
  }

  @Override
  public Validator getValidator() {
    return validator;
  }

  @Override
  public ValidatorContext usingContext() {
    return this;
  }

  @Override
  public MessageInterpolator getMessageInterpolator() {
    return config.getMessageInterpolator();
  }

  @Override
  public TraversableResolver getTraversableResolver() {
    return config.getTraversableResolver();
  }

  @Override
  public ConstraintValidatorFactory getConstraintValidatorFactory() {
    return config.getConstraintValidatorFactory();
  }

  @Override
  public ParameterNameProvider getParameterNameProvider() {
    return config.getParameterNameProvider();
  }

  @Override
  public <T> T unwrap(Class<T> type) {
    if (type.isAssignableFrom(this.getClass())) {
      return type.cast(this);
    }
    throw new IllegalArgumentException("cannot wrap to " + type);
  }

  @Override
  public void close() {
    validator.close();
    if (nb.decrementAndGet() == 0) {
      validator.stopJmx();
    }
  }

  @Override
  public ValidatorContext messageInterpolator(MessageInterpolator messageInterpolator) {
    return new ValidatorFactoryImpl(config.messageInterpolator(messageInterpolator));
  }

  @Override
  public ValidatorContext traversableResolver(TraversableResolver traversableResolver) {
    return new ValidatorFactoryImpl(config.traversableResolver(traversableResolver));
  }

  @Override
  public ValidatorContext constraintValidatorFactory(ConstraintValidatorFactory factory) {
    return new ValidatorFactoryImpl(config.constraintValidatorFactory(factory));
  }

  @Override
  public ValidatorContext parameterNameProvider(ParameterNameProvider parameterNameProvider) {
    return new ValidatorFactoryImpl(config.parameterNameProvider(parameterNameProvider));
  }

}
