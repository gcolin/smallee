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
package net.gcolin.di.atinject.web;

import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.InstanceCreator;

/**
 * ApplicationDecorateProvider.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ApplicationDecorateProvider<T> extends ApplicationProvider<T> {

  private AbstractProvider<T> provider;

  public ApplicationDecorateProvider(AbstractProvider<T> provider) {
    super(provider.getType(), provider.getGenericType(), provider.getResolvedType(),
        provider.getResolvedGenericType(), provider.getEnvironment());
    this.provider = provider;
  }

  @Override
  public InstanceCreator getBuilder() {
    return provider.getBuilder();
  }

  @Override
  public void setBuilder(InstanceCreator builder) {
    provider.setBuilder(builder);
  }

}
