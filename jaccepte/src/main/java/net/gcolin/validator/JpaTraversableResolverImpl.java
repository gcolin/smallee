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

import java.lang.annotation.ElementType;

import javax.persistence.Persistence;
import javax.validation.Path;
import javax.validation.Path.Node;
import javax.validation.TraversableResolver;

/**
 * A TraversableResolver implementation for jpa.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JpaTraversableResolverImpl implements TraversableResolver {

  @Override
  public boolean isReachable(Object traversableObject, Node traversableProperty,
      Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
    if (traversableObject == null) {
      return true;
    }

    return Persistence.getPersistenceUtil().isLoaded(traversableObject,
        traversableProperty.getName());
  }

  @Override
  public boolean isCascadable(Object traversableObject, Node traversableProperty,
      Class<?> rootBeanType, Path pathToTraversableObject, ElementType elementType) {
    return true;
  }

}
