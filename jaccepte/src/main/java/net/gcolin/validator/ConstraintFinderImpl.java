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

import net.gcolin.common.collection.Collections2;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.validation.groups.Default;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ElementDescriptor.ConstraintFinder;
import javax.validation.metadata.Scope;

/**
 * A ConstraintFinder implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ConstraintFinderImpl implements ConstraintFinder {

  public static final Set<Class<?>> DEFAULT_GROUP =
      Collections.unmodifiableSet(Collections2.toSet(Default.class));

  private Set<Class<?>> groups = DEFAULT_GROUP;
  private Scope scope = Scope.HIERARCHY;
  private Set<ElementType> types = new HashSet<>();
  private Set<ConstraintDescriptor<?>> contraints;
  private Set<ConstraintDescriptor<?>> tmpcontraints;
  private Class<?> elementType;

  /**
   * Create a ConstraintFinder.
   * 
   * @param contraints many constraints
   * @param elementType the constrained type
   */
  public ConstraintFinderImpl(Set<ConstraintDescriptor<?>> contraints, Class<?> elementType) {
    super();
    this.contraints = contraints;
    this.elementType = elementType;
  }

  @Override
  public ConstraintFinder unorderedAndMatchingGroups(Class<?>... groups) {
    this.groups = new HashSet<Class<?>>(Arrays.asList(groups));
    tmpcontraints = null;
    return this;
  }

  @Override
  public ConstraintFinder lookingAt(Scope scope) {
    this.scope = scope;
    tmpcontraints = null;
    return this;
  }

  @Override
  public ConstraintFinder declaredOn(ElementType... types) {
    this.types = new HashSet<>(Arrays.asList(types));
    tmpcontraints = null;
    return this;
  }

  @Override
  public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
    if (tmpcontraints == null) {
      tmpcontraints = new HashSet<>();
      for (ConstraintDescriptor<?> c : contraints) {
        if (containsOneGroup(c) && hasScope(c) && hasType(c)) {
          tmpcontraints.add(c);
        }
      }
    }
    return tmpcontraints;
  }

  private boolean hasScope(ConstraintDescriptor<?> descriptor) {
    return scope == Scope.HIERARCHY
        || ((ConstraintDescriptorImpl<?>) descriptor).getDeclaringClass() == elementType;
  }

  private boolean hasType(ConstraintDescriptor<?> descriptor) {
    return types.isEmpty() || types.contains(((ConstraintDescriptorImpl<?>) descriptor).getType());
  }

  private boolean containsOneGroup(ConstraintDescriptor<?> descriptor) {
    for (Class<?> group : groups) {
      if (descriptor.getGroups().contains(group)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean hasConstraints() {
    return !getConstraintDescriptors().isEmpty();
  }

}
