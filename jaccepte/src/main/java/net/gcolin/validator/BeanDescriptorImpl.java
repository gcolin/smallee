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

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ParameterNameProvider;
import javax.validation.Valid;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.MethodType;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.PropertyDescriptor;

/**
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BeanDescriptorImpl extends AbstractElementDescriptor implements BeanDescriptor {

  public static final String PARAM_INDEX = "@paramIndex";
  private Set<PropertyDescriptor> propertyDescriptors = new HashSet<>();
  private Set<MethodDescriptor> methodDescriptors = new HashSet<>();
  private Set<ConstructorDescriptor> constructorDescriptors = new HashSet<>();
  private boolean hasValid;
  private static Map<String, List<Class<?>>> validators = null;

  static {
    validators = new HashMap<>();
    for (ConstraintValidator c : ServiceLoader.load(ConstraintValidator.class)) {
      Class<?> clazz = c.getClass();
      List<Class<?>> listTypes =
          Reflect.getTypeArguments(ConstraintValidator.class, (Class) clazz, null);
      String key = listTypes.get(0) + "," + Reflect.toNonPrimitiveEquivalent(listTypes.get(1));
      List<Class<?>> list = validators.get(key);
      if (list == null) {
        list = new ArrayList<>();
        validators.put(key, list);
      }
      list.add(clazz);
    }
  }

  /**
   * Create a bean descriptor.
   * 
   * @param clazz the bean class
   * @param config the configuration
   */
  public BeanDescriptorImpl(Class<?> clazz, ValidatorConfigurationImpl config) {
    super(clazz);
    setContraints(new HashSet<>());
    Class<?> cl = clazz;

    ParameterNameProvider pnameProvider = config.getParameterNameProvider();

    List<ConstraintDescriptor<?>> contraintsProperty = new ArrayList<>();
    while (cl != Object.class) {
      findConstraints(getConstraintDescriptors(), cl.getAnnotations(), ElementType.TYPE, null, cl,
          config);
      for (Class<?> intf : cl.getInterfaces()) {
        findConstraints(getConstraintDescriptors(), intf.getAnnotations(), ElementType.TYPE, null,
            intf, config);
      }

      findFieldConstraints(config, cl, contraintsProperty);
      findMethodConstraints(config, cl, pnameProvider, contraintsProperty);
      findConstructorConstraints(config, cl, pnameProvider, contraintsProperty);

      cl = cl.getSuperclass();
    }
  }

  private void findConstructorConstraints(ValidatorConfigurationImpl config, Class<?> beanClass,
      ParameterNameProvider pnameProvider, List<ConstraintDescriptor<?>> contraintsProperty) {
    for (Constructor<?> m : beanClass.getDeclaredConstructors()) {
      findConstraints(contraintsProperty, m.getAnnotations(), ElementType.CONSTRUCTOR, m, beanClass,
          config);

      List<String> names = pnameProvider.getParameterNames(m);
      List<Integer> validParameters = findValidParameters(config, beanClass, contraintsProperty, m,
          m.getParameterAnnotations());

      boolean hasValid = m.isAnnotationPresent(Valid.class);

      if (!contraintsProperty.isEmpty() || !validParameters.isEmpty() || hasValid) {
        ConstructorDescriptorImpl constructorDescriptor =
            (ConstructorDescriptorImpl) getConstraintsForConstructor(m.getParameterTypes());
        if (constructorDescriptor == null) {
          constructorDescriptor = new ConstructorDescriptorImpl(m);
          constructorDescriptors.add(constructorDescriptor);
        }
        if (hasValid) {
          constructorDescriptor.setHasValid(hasValid);
        }
        constructorDescriptor.update(m);
        constructorDescriptor.getConstraintDescriptors().addAll(contraintsProperty);
        for (ConstraintDescriptor<?> cdd : contraintsProperty) {
          addConstructorConstraint(m, names, constructorDescriptor, cdd);
        }

        for (Integer validParameterIndex : validParameters) {
          ParameterDescriptor selected = findParameterDescriptor(m.getParameterTypes(), names,
              constructorDescriptor, validParameterIndex);
          ((ParameterDescriptorImpl) selected).update(m);
        }
        getConstraintDescriptors().addAll(contraintsProperty);
        contraintsProperty.clear();
      }
    }
  }

  private List<Integer> findValidParameters(ValidatorConfigurationImpl config, Class<?> beanClass,
      List<ConstraintDescriptor<?>> contraintsProperty, Member member, Annotation[][] allma) {
    List<Integer> validParameters = new ArrayList<Integer>();
    for (int i = 0; i < allma.length; i++) {
      if (Reflect.getAnnotation(allma[i], Valid.class) != null) {
        validParameters.add(i);
      }
      findConstraints(contraintsProperty, allma[i], ElementType.PARAMETER, member, beanClass,
          buildParamPostConstruct(i), config);
    }
    return validParameters;
  }

  private void addConstructorConstraint(Constructor<?> constructor, List<String> names,
      ConstructorDescriptorImpl constructorDescriptor, ConstraintDescriptor<?> cdd) {
    ConstraintDescriptorImpl<?> cd = (ConstraintDescriptorImpl<?>) cdd;
    if (cd.getType() == ElementType.CONSTRUCTOR) {
      if (constructor.getParameterCount() == 0) {
        constructorDescriptor.getReturnValueDescriptor().getConstraintDescriptors().add(cd);
      } else {
        constructorDescriptor.getCrossParameterDescriptor().getConstraintDescriptors().add(cd);
      }
    } else if (cd.getType() == ElementType.PARAMETER) {
      int index = (Integer) cd.getAttributes().get(PARAM_INDEX);
      ParameterDescriptor selected = findParameterDescriptor(constructor.getParameterTypes(), names,
          constructorDescriptor, index);
      selected.getConstraintDescriptors().add(cd);
      ((ParameterDescriptorImpl) selected).update(constructor);
    }
  }

  private void findMethodConstraints(ValidatorConfigurationImpl config, Class<?> beanClass,
      ParameterNameProvider pnameProvider, List<ConstraintDescriptor<?>> contraintsProperty) {
    for (Method m : beanClass.getDeclaredMethods()) {
      MethodType methodType = Reflect.isGetter(m) ? MethodType.GETTER : MethodType.NON_GETTER;

      boolean hasValidAnnotation = m.isAnnotationPresent(Valid.class);
      if (hasValidAnnotation) {
        this.hasValid = true;
      }

      findConstraints(contraintsProperty, m.getAnnotations(), ElementType.METHOD, m, beanClass,
          config);
      if ((!contraintsProperty.isEmpty() || hasValidAnnotation)
          && methodType == MethodType.GETTER) {
        PropertyDescriptorImpl propertyDescriptor =
            (PropertyDescriptorImpl) getConstraintsForProperty(Reflect.getPropertyName(m));
        if (propertyDescriptor == null) {
          propertyDescriptor = new PropertyDescriptorImpl(m.getReturnType());
          propertyDescriptors.add(propertyDescriptor);
        }
        propertyDescriptor.update(m);
        propertyDescriptor.getConstraintDescriptors().addAll(contraintsProperty);

      }

      List<String> names = pnameProvider.getParameterNames(m);
      List<Integer> validParameters = findValidParameters(config, beanClass, contraintsProperty, m,
          m.getParameterAnnotations());

      if (!contraintsProperty.isEmpty() || !validParameters.isEmpty() || hasValid) {
        MethodDescriptorImpl methodDescriptor =
            (MethodDescriptorImpl) getConstraintsForMethod(m.getName(), m.getParameterTypes());
        if (methodDescriptor == null) {
          methodDescriptor = new MethodDescriptorImpl(m.getReturnType(), m.getName(), methodType,
              m.getParameterTypes());
          methodDescriptors.add(methodDescriptor);
        }
        methodDescriptor.update(m);
        methodDescriptor.getConstraintDescriptors().addAll(contraintsProperty);
        for (ConstraintDescriptor<?> cdd : contraintsProperty) {
          addMethodConstraint(m, names, methodDescriptor, cdd);
        }
        for (Integer validParameterIndex : validParameters) {
          ParameterDescriptor selected = findParameterDescriptor(m.getParameterTypes(), names,
              methodDescriptor, validParameterIndex);
          ((ParameterDescriptorImpl) selected).update(m);
        }
        getConstraintDescriptors().addAll(contraintsProperty);
        contraintsProperty.clear();
      }
    }
  }

  private void addMethodConstraint(Method method, List<String> names,
      MethodDescriptorImpl methodDescriptor, ConstraintDescriptor<?> cdd) {
    ConstraintDescriptorImpl<?> cd = (ConstraintDescriptorImpl<?>) cdd;
    if (cd.getType() == ElementType.METHOD) {
      if (methodDescriptor.getMethodType() == MethodType.GETTER) {
        methodDescriptor.getReturnValueDescriptor().getConstraintDescriptors().add(cd);
      } else {
        methodDescriptor.getCrossParameterDescriptor().getConstraintDescriptors().add(cd);
      }
    } else if (cd.getType() == ElementType.PARAMETER) {
      int index = (Integer) cd.getAttributes().get(PARAM_INDEX);
      ParameterDescriptor selected =
          findParameterDescriptor(method.getParameterTypes(), names, methodDescriptor, index);
      selected.getConstraintDescriptors().add(cd);
      ((ParameterDescriptorImpl) selected).update(method);
    }
  }

  private void findFieldConstraints(ValidatorConfigurationImpl config, Class<?> beanClass,
      List<ConstraintDescriptor<?>> contraintsProperty) {
    for (Field f : beanClass.getDeclaredFields()) {
      boolean hasValidAnnotation = f.isAnnotationPresent(Valid.class);
      if (hasValidAnnotation) {
        this.hasValid = true;
      }
      findConstraints(contraintsProperty, f.getAnnotations(), ElementType.FIELD, f, beanClass,
          config);
      if (!contraintsProperty.isEmpty() || hasValidAnnotation) {
        PropertyDescriptorImpl propertyDescriptor =
            (PropertyDescriptorImpl) getConstraintsForProperty(f.getName());
        if (propertyDescriptor == null) {
          propertyDescriptor = new PropertyDescriptorImpl(f.getType());
          propertyDescriptors.add(propertyDescriptor);
        }
        propertyDescriptor.update(f);
        propertyDescriptor.getConstraintDescriptors().addAll(contraintsProperty);
        getConstraintDescriptors().addAll(contraintsProperty);
        contraintsProperty.clear();
      }
    }
  }

  private ParameterDescriptor findParameterDescriptor(Class<?>[] argTypes, List<String> argNames,
      AbstractExecutableDescriptor descriptor, int index) {
    ParameterDescriptor selected = null;
    String name = argNames.get(index);
    for (ParameterDescriptor pd : descriptor.getParameterDescriptors()) {
      if (pd.getIndex() == index && pd.getName().equals(name)) {
        selected = pd;
        break;
      }
    }
    if (selected == null) {
      selected = new ParameterDescriptorImpl(argTypes[index], index, argNames.get(index));
      int insertIndex = Math.min(index, descriptor.getParameterDescriptors().size());
      descriptor.getParameterDescriptors().add(insertIndex, selected);
    }
    return selected;
  }

  private Consumer<ConstraintDescriptorImpl<Annotation>> buildParamPostConstruct(int index) {
    return x -> x.getAttributes().put(PARAM_INDEX, index);
  }

  private void findConstraints(Collection<ConstraintDescriptor<?>> contraints, Annotation[] array,
      ElementType type, Member member, Class<?> declaringClass,
      Consumer<ConstraintDescriptorImpl<Annotation>> postConstruct,
      ValidatorConfigurationImpl configuration) {
    for (Annotation a : array) {
      if (a.annotationType().isAnnotationPresent(Constraint.class)) {
        ConstraintDescriptorImpl<Annotation> cd =
            new ConstraintDescriptorImpl<Annotation>(a, type, member, declaringClass);
        if (postConstruct != null) {
          postConstruct.accept(cd);
        }
        contraints.add(cd);
        fillConstraintValidator(cd, configuration.getConstraintValidatorFactory());
      }
    }
  }

  private void findConstraints(Collection<ConstraintDescriptor<?>> contraints, Annotation[] array,
      ElementType type, Member member, Class<?> declaringClass,
      ValidatorConfigurationImpl configuration) {
    findConstraints(contraints, array, type, member, declaringClass, null, configuration);
  }

  private void fillConstraintValidator(ConstraintDescriptorImpl<Annotation> cd,
      ConstraintValidatorFactory constraintValidatorFactory) {
    String keyPrefix = cd.getAnnotation().annotationType() + ",";
    Class<?> constraintType;
    if (cd.getType() == ElementType.PARAMETER) {
      constraintType = Reflect.toNonPrimitiveEquivalent(Reflect.getType(cd.getMember(),
          (Integer) cd.getAttributes().get(BeanDescriptorImpl.PARAM_INDEX)));
    } else if (cd.getType() == ElementType.TYPE) {
      constraintType = Reflect.toNonPrimitiveEquivalent(cd.getDeclaringClass());
    } else {
      constraintType = Reflect.toNonPrimitiveEquivalent(Reflect.getType(cd.getMember()));
    }
    Class<?> type = constraintType;
    while (type != null) {
      if (!type.isInterface()) {
        for (Class<?> intf : type.getInterfaces()) {
          if (fillConstraintValidator(cd, keyPrefix + intf, constraintValidatorFactory)) {
            return;
          }
        }
      }
      if (fillConstraintValidator(cd, keyPrefix + type, constraintValidatorFactory)) {
        return;
      } else if (type.getSuperclass() == null && type != Object.class) {
        type = Object.class;
      } else {
        type = type.getSuperclass();
      }
    }

    if (constraintType != null && constraintType.isArray()
        && !constraintType.getComponentType().isPrimitive()) {
      fillConstraintValidator(cd, keyPrefix + Object[].class, constraintValidatorFactory);
    }
  }

  private boolean fillConstraintValidator(ConstraintDescriptorImpl<Annotation> cd, String key,
      ConstraintValidatorFactory constraintValidatorFactory) {
    List<Class<?>> validatorClasses = validators.get(key);
    if (validatorClasses == null && key.endsWith(",class java.lang.Object")
        && cd.getConstraintValidators().isEmpty()) {
      cd.getConstraintValidators().addAll(cd.getConstraintValidatorClasses().stream()
          .map(Reflect::newInstance).collect(Collectors.toList()));
    } else if (validatorClasses != null) {
      for (Class<?> validatorClass : validatorClasses) {
        cd.getConstraintValidatorClasses().add((Class) validatorClass);
        ConstraintValidator<Annotation, Object> validator =
            (ConstraintValidator<Annotation, Object>) constraintValidatorFactory
                .getInstance((Class) validatorClass);
        validator.initialize(cd.getAnnotation());
        cd.getConstraintValidators().add(validator);
      }
    }
    return validatorClasses != null;
  }

  @Override
  public boolean isBeanConstrained() {
    return hasConstraints() || hasValid;
  }

  @Override
  public PropertyDescriptor getConstraintsForProperty(String propertyName) {
    for (PropertyDescriptor p : propertyDescriptors) {
      if (propertyName.equals(p.getPropertyName())) {
        return p;
      }
    }
    return null;
  }

  @Override
  public Set<PropertyDescriptor> getConstrainedProperties() {
    return propertyDescriptors;
  }

  @Override
  public MethodDescriptor getConstraintsForMethod(String methodName, Class<?>... parameterTypes) {
    for (MethodDescriptor m : methodDescriptors) {
      MethodDescriptorImpl md = (MethodDescriptorImpl) m;
      if (methodName.equals(md.getName())
          && Arrays.equals(((MethodDescriptorImpl) m).getParameterTypes(), parameterTypes)) {
        return m;
      }
    }
    return null;
  }

  @Override
  public Set<MethodDescriptor> getConstrainedMethods(MethodType methodType,
      MethodType... methodTypes) {
    boolean getter =
        methodType == MethodType.GETTER || Arrays.asList(methodTypes).contains(MethodType.GETTER);
    boolean nongetter = methodType == MethodType.NON_GETTER
        || Arrays.asList(methodTypes).contains(MethodType.NON_GETTER);
    if (getter && nongetter) {
      return methodDescriptors;
    } else if (getter) {
      return methodDescriptors.stream()
          .filter(x -> ((MethodDescriptorImpl) x).getMethodType() == MethodType.GETTER)
          .collect(Collectors.toSet());
    } else {
      return methodDescriptors.stream()
          .filter(x -> ((MethodDescriptorImpl) x).getMethodType() == MethodType.NON_GETTER)
          .collect(Collectors.toSet());
    }
  }

  @Override
  public ConstructorDescriptor getConstraintsForConstructor(Class<?>... parameterTypes) {
    for (ConstructorDescriptor cd : constructorDescriptors) {
      if (Arrays.equals(parameterTypes, ((ConstructorDescriptorImpl) cd).getParameterTypes())) {
        return cd;
      }
    }
    return null;
  }

  @Override
  public Set<ConstructorDescriptor> getConstrainedConstructors() {
    return constructorDescriptors;
  }

  @Override
  public String toString() {
    return "BeanDescriptorImpl [propertyDescriptors=" + propertyDescriptors + ", methodDescriptors="
        + methodDescriptors + ", constructorDescriptors=" + constructorDescriptors
        + ", isBeanConstrained()=" + isBeanConstrained() + ", getElementClass()="
        + getElementClass() + "]";
  }

}
