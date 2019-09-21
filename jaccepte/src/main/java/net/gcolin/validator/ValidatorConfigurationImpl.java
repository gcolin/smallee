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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableType;
import javax.validation.spi.ConfigurationState;

/**
 * A Configuration implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ValidatorConfigurationImpl
    implements
      Configuration<ValidatorConfigurationImpl>,
      BootstrapConfiguration,
      Cloneable {

  private boolean ignoreXml;
  private MessageInterpolator messageInterpolator;
  private TraversableResolver traversableResolver;
  private ConstraintValidatorFactory constraintValidatorFactory;
  private ParameterNameProvider parameterNameProvider;
  private Map<String, String> properties = new HashMap<>();
  private MessageInterpolator defaultmessageInterpolator = new MessageInterpolatorImpl();
  private TraversableResolver defaulttraversableResolver = new TraversableResolverImpl();
  private ConstraintValidatorFactory defaultconstraintValidatorFactory =
      new ConstraintValidatorFactoryImpl();
  private ParameterNameProvider defaultparameterNameProvider = new ParameterNameProviderImpl();
  private ValidatorFactoryImpl validationFactory;

  /**
   * Create a ValidatorConfigurationImpl.
   */
  public ValidatorConfigurationImpl() {
    if (Reflect.exists("javax.persistence.Persistence",
        ValidatorConfigurationImpl.class.getClassLoader())) {
      defaulttraversableResolver = new JpaTraversableResolverImpl();
    }
  }

  @Override
  public ValidatorConfigurationImpl clone() {
    try {
      return (ValidatorConfigurationImpl) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new ValidationException(ex);
    }
  }

  @Override
  public ValidatorConfigurationImpl ignoreXmlConfiguration() {
    if (!ignoreXml) {
      ValidatorConfigurationImpl conf = clone();
      conf.ignoreXml = true;
      conf.validationFactory = null;
      return conf;
    }
    return this;
  }

  @Override
  public ValidatorConfigurationImpl messageInterpolator(MessageInterpolator interpolator) {
    if (interpolator != messageInterpolator) {
      ValidatorConfigurationImpl conf = clone();
      conf.messageInterpolator = interpolator;
      conf.validationFactory = null;
      return conf;
    }
    return this;
  }

  @Override
  public ValidatorConfigurationImpl traversableResolver(TraversableResolver resolver) {
    if (resolver != traversableResolver) {
      ValidatorConfigurationImpl conf = clone();
      conf.traversableResolver = resolver;
      conf.validationFactory = null;
      return conf;
    }
    return this;
  }

  @Override
  public ValidatorConfigurationImpl constraintValidatorFactory(
      ConstraintValidatorFactory constraintValidatorFactory) {
    if (this.constraintValidatorFactory != constraintValidatorFactory) {
      ValidatorConfigurationImpl conf = clone();
      conf.constraintValidatorFactory = constraintValidatorFactory;
      conf.validationFactory = null;
      return conf;
    }
    return this;
  }

  @Override
  public ValidatorConfigurationImpl parameterNameProvider(
      ParameterNameProvider parameterNameProvider) {
    if (this.parameterNameProvider != parameterNameProvider) {
      ValidatorConfigurationImpl conf = clone();
      conf.parameterNameProvider = parameterNameProvider;
      conf.validationFactory = null;
      return conf;
    }
    return this;
  }

  @Override
  public ValidatorConfigurationImpl addMapping(InputStream stream) {
    if (!ignoreXml) {
      throw new UnsupportedOperationException();
    }
    return this;
  }

  @Override
  public ValidatorConfigurationImpl addProperty(String name, String value) {
    if (!Objects.equals(properties.get(name), value)) {
      ValidatorConfigurationImpl conf = clone();
      conf.properties = new HashMap<>();
      conf.properties.putAll(properties);
      conf.properties.put(name, value);
      conf.validationFactory = null;
      return conf;
    }
    return this;
  }

  @Override
  public MessageInterpolator getDefaultMessageInterpolator() {
    return defaultmessageInterpolator;
  }

  @Override
  public TraversableResolver getDefaultTraversableResolver() {
    return defaulttraversableResolver;
  }

  @Override
  public ConstraintValidatorFactory getDefaultConstraintValidatorFactory() {
    return defaultconstraintValidatorFactory;
  }

  @Override
  public ParameterNameProvider getDefaultParameterNameProvider() {
    return defaultparameterNameProvider;
  }

  @Override
  public BootstrapConfiguration getBootstrapConfiguration() {
    return this;
  }

  @Override
  public ValidatorFactory buildValidatorFactory() {
    if (validationFactory == null) {
      ValidatorImpl.LOG.debug("create Juikito ValidatorFactory");
      validationFactory = new ValidatorFactoryImpl(this);
    } else {
      validationFactory.incr();
    }
    return validationFactory;
  }

  @Override
  public String getDefaultProviderClassName() {
    return ValidatorProviderImpl.class.getName();
  }

  @Override
  public String getConstraintValidatorFactoryClassName() {
    return getConstraintValidatorFactory().getClass().getName();
  }

  @Override
  public String getMessageInterpolatorClassName() {
    return getMessageInterpolator().getClass().getName();
  }

  @Override
  public String getTraversableResolverClassName() {
    return getTraversableResolver().getClass().getName();
  }

  @Override
  public String getParameterNameProviderClassName() {
    return getParameterNameProvider().getClass().getName();
  }

  @Override
  public Set<String> getConstraintMappingResourcePaths() {
    return new HashSet<String>();
  }

  @Override
  public boolean isExecutableValidationEnabled() {
    return false;
  }

  @Override
  public Set<ExecutableType> getDefaultValidatedExecutableTypes() {
    return new HashSet<ExecutableType>(
        Arrays.asList(ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS));
  }

  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  void set(ConfigurationState configurationState) {
    if (configurationState.isIgnoreXmlConfiguration()) {
      ignoreXmlConfiguration();
    } else {
      ignoreXml = false;
    }
    constraintValidatorFactory(configurationState.getConstraintValidatorFactory());
    Set<InputStream> instreams = configurationState.getMappingStreams();
    if (instreams != null) {
      for (InputStream in : instreams) {
        addMapping(in);
      }
    }
    messageInterpolator(configurationState.getMessageInterpolator());
    parameterNameProvider(configurationState.getParameterNameProvider());
    traversableResolver(configurationState.getTraversableResolver());
  }

  public MessageInterpolator getMessageInterpolator() {
    return messageInterpolator == null ? getDefaultMessageInterpolator() : messageInterpolator;
  }

  public TraversableResolver getTraversableResolver() {
    return traversableResolver == null ? getDefaultTraversableResolver() : traversableResolver;
  }

  ConstraintValidatorFactory getConstraintValidatorFactory() {
    return constraintValidatorFactory == null
        ? getDefaultConstraintValidatorFactory()
        : constraintValidatorFactory;
  }

  ParameterNameProvider getParameterNameProvider() {
    return parameterNameProvider == null
        ? getDefaultParameterNameProvider()
        : parameterNameProvider;
  }

}
