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

import java.util.ArrayList;
import java.util.List;

import javax.validation.Configuration;
import javax.validation.ValidatorFactory;
import javax.validation.spi.BootstrapState;
import javax.validation.spi.ConfigurationState;
import javax.validation.spi.ValidationProvider;

/**
 * A ValidatorProviderImpl implementation. The entry point from the API.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ValidatorProviderImpl implements ValidationProvider<ValidatorConfigurationImpl> {

  private static ValidatorConfigurationImpl DEFAULT_CONFIG = new ValidatorConfigurationImpl();
  private static final List<ValidatorConfigurationImpl> ALLCONFIGS = new ArrayList<>();

  static {
    ALLCONFIGS.add(DEFAULT_CONFIG);
  }

  /**
   * Clear the provider.
   */
  public static void clear() {
    for (ValidatorConfigurationImpl c : ALLCONFIGS) {
      c.buildValidatorFactory().close();
    }
    ALLCONFIGS.clear();
    ALLCONFIGS.add(DEFAULT_CONFIG = new ValidatorConfigurationImpl());
  }

  @Override
  public ValidatorConfigurationImpl createSpecializedConfiguration(BootstrapState state) {
    return DEFAULT_CONFIG;
  }

  @Override
  public Configuration<?> createGenericConfiguration(BootstrapState state) {
    return createSpecializedConfiguration(state);
  }

  @Override
  public ValidatorFactory buildValidatorFactory(ConfigurationState configurationState) {
    ValidatorConfigurationImpl config = new ValidatorConfigurationImpl();
    config.set(configurationState);
    return config.buildValidatorFactory();
  }
}

