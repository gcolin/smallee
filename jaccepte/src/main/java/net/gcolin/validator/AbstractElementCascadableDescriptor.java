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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.validation.metadata.CascadableDescriptor;
import javax.validation.metadata.GroupConversionDescriptor;

/**
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class AbstractElementCascadableDescriptor extends AbstractElementDescriptor
    implements
      CascadableDescriptor {

  private Set<GroupConversionDescriptor> groupConversionDescriptors = new HashSet<>();
  protected boolean cascaded;

  public AbstractElementCascadableDescriptor(Class<?> elementClass) {
    super(elementClass);
  }

  public void setCascaded() {
    this.cascaded = true;
  }

  void update(Field field) {
    cascaded = cascaded || field.isAnnotationPresent(Valid.class);
    addConvertGroups(field.getAnnotationsByType(ConvertGroup.class), groupConversionDescriptors);
    addConvertGroups(field.getAnnotationsByType(ConvertGroup.List.class),
        groupConversionDescriptors);
  }

  void update(Method method) {
    cascaded = cascaded || method.isAnnotationPresent(Valid.class);
    addConvertGroups(method.getAnnotationsByType(ConvertGroup.class), groupConversionDescriptors);
    addConvertGroups(method.getAnnotationsByType(ConvertGroup.List.class),
        groupConversionDescriptors);
  }

  void addConvertGroups(ConvertGroup[] groups,
      Set<GroupConversionDescriptor> groupConversionDescriptors) {
    if (groups.length > 0) {
      for (ConvertGroup cg : groups) {
        groupConversionDescriptors.add(new GroupConversionDescriptorImpl(cg));
      }
    }
  }

  void addConvertGroups(ConvertGroup.List[] groups,
      Set<GroupConversionDescriptor> groupConversionDescriptors) {
    if (groups.length > 0) {
      for (ConvertGroup.List cg : groups) {
        addConvertGroups(cg.value(), groupConversionDescriptors);
      }
    }
  }

  @Override
  public boolean isCascaded() {
    return cascaded;
  }

  @Override
  public boolean hasConstraints() {
    return isCascaded() || !getConstraintDescriptors().isEmpty();
  }

  @Override
  public Set<GroupConversionDescriptor> getGroupConversions() {
    return groupConversionDescriptors;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [" + getElementClass() + "->"
        + getConstraintDescriptors().size() + (cascaded ? "+cascade" : "") + "]";
  }

}
