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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Constraint;
import javax.validation.ConstraintTarget;
import javax.validation.ConstraintValidator;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.ValidationException;
import javax.validation.metadata.ConstraintDescriptor;

/**
 * A ConstraintDescriptor implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ConstraintDescriptorImpl<T extends Annotation> implements ConstraintDescriptor<T> {

  private T annotation;
  private Set<Class<?>> groups = ConstraintFinderImpl.DEFAULT_GROUP;
  private Set<Class<? extends Payload>> payload = new HashSet<>();
  private Map<String, Object> attributes = new HashMap<>();
  private List<Class<? extends ConstraintValidator<T, ?>>> constraintValidatorClasses =
      new ArrayList<>();
  private List<ConstraintValidator<T, ?>> constraintValidators = new ArrayList<>();
  private String messageTemplate;
  private Set<ConstraintDescriptor<?>> composingConstraints = new HashSet<>();
  private boolean reportAsSingleViolation;
  private ElementType type;
  private Member member;
  private Class<?> declaringClass;

  /**
   * Create a ConstraintDescriptor.
   * 
   * @param annotation the constraint annotation
   * @param type the location of the constraint
   * @param member the associated member
   * @param declaringClass the class of the member
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public ConstraintDescriptorImpl(T annotation, ElementType type, Member member,
      Class<?> declaringClass) {
    this.declaringClass = declaringClass;
    this.annotation = annotation;
    this.type = type;
    this.member = member;
    Constraint contraint = annotation.annotationType().getAnnotation(Constraint.class);
    constraintValidatorClasses.addAll((Collection) Arrays.asList(contraint.validatedBy()));

    reportAsSingleViolation =
        annotation.annotationType().isAnnotationPresent(ReportAsSingleViolation.class);
    try {
      for (Method m : annotation.annotationType().getMethods()) {
        if (m.getParameterCount() == 0) {
          String name = m.getName();
          Object value = m.invoke(annotation);
          attributes.put(name, value);

          parseAnnotationValue(m, name, value);
        }
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new ValidationException(ex);
    }
    for (Annotation a : annotation.annotationType().getAnnotations()) {
      if (a.annotationType().isAnnotationPresent(Constraint.class)) {
        composingConstraints.add(new ConstraintDescriptorImpl(a, type, member, declaringClass));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void parseAnnotationValue(Method met, String name, Object value) {
    if ("groups".equals(name) && met.getReturnType() == Class[].class) {
      for (Class<?> c : (Class[]) value) {
        if (groups == ConstraintFinderImpl.DEFAULT_GROUP) {
          groups = new HashSet<Class<?>>();
        }
        groups.add(c);
      }
    } else if ("message".equals(name) && met.getReturnType() == String.class) {
      messageTemplate = (String) value;
    } else if ("payload".equals(name) && met.getReturnType() == Class[].class) {
      for (Class<?> c : (Class[]) value) {
        payload.add((Class<? extends Payload>) c);
      }
    }
  }

  public ElementType getType() {
    return type;
  }

  public Member getMember() {
    return member;
  }

  private boolean isReturnConstraintTarget() {
    return type != ElementType.METHOD && ((Method) member).getParameterCount() == 0
        || type != ElementType.CONSTRUCTOR && ((Constructor<?>) member).getParameterCount() == 0;
  }

  private boolean isParameterConstraintTarget() {
    return type != ElementType.METHOD && ((Method) member).getParameterCount() != 0
        || type != ElementType.CONSTRUCTOR && ((Constructor<?>) member).getParameterCount() != 0;
  }

  @Override
  public T getAnnotation() {
    return annotation;
  }

  @Override
  public String getMessageTemplate() {
    return messageTemplate;
  }

  @Override
  public Set<Class<?>> getGroups() {
    return groups;
  }

  @Override
  public Set<Class<? extends Payload>> getPayload() {
    return payload;
  }

  @Override
  public ConstraintTarget getValidationAppliesTo() {
    if (isParameterConstraintTarget()) {
      return ConstraintTarget.PARAMETERS;
    } else if (isReturnConstraintTarget()) {
      return ConstraintTarget.RETURN_VALUE;
    } else {
      return ConstraintTarget.IMPLICIT;
    }
  }

  @Override
  public List<Class<? extends ConstraintValidator<T, ?>>> getConstraintValidatorClasses() {
    return constraintValidatorClasses;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Set<ConstraintDescriptor<?>> getComposingConstraints() {
    return composingConstraints;
  }

  @Override
  public boolean isReportAsSingleViolation() {
    return reportAsSingleViolation;
  }

  public Class<?> getDeclaringClass() {
    return declaringClass;
  }

  public List<ConstraintValidator<T, ?>> getConstraintValidators() {
    return constraintValidators;
  }

  public void setConstraintValidators(List<ConstraintValidator<T, ?>> constraintValidators) {
    this.constraintValidators = constraintValidators;
  }

}
