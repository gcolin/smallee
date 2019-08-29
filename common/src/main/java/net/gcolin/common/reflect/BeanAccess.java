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

package net.gcolin.common.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility class accessing to object properties
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class BeanAccess {

  private static final String CANNOT_FIND_METHOD_OR_FIELD_FOR_PROPERTY =
      "cannot find method or field for property ";
  private static final String CANNOT_GET_THE_VALUE_OF_THE_FIELD =
      "cannot get the value of the field ";
  private static final String NO_NAME_SPECIFIED_FOR_BEAN_CLASS =
      "No name specified for bean class '";
  private static final String NO_BEAN_SPECIFIED = "No bean specified";

  private BeanAccess() {}

  /**
   * Set property value. Supports deep hierarchy level (prop1.prop2.prop3)
   * 
   * @param bean The object which hold the property
   * @param name The property name
   * @param obj The property value
   * @throws IllegalArgumentException if the bean is null or the name is null or the property does
   *         not exist
   */
  @SuppressWarnings("unchecked")
  public static void setProperty(Object bean, String name, Object obj) {
    if (bean == null) {
      throw new IllegalArgumentException(NO_BEAN_SPECIFIED);
    }
    if (name == null) {
      throw new IllegalArgumentException(NO_NAME_SPECIFIED_FOR_BEAN_CLASS + bean.getClass() + "'");
    }

    Object ref = bean;

    String[] parts = name.split("\\.");

    for (int i = 0; i < parts.length - 1; i++) {
      ref = getProperty0(ref, parts[i]);
    }

    setter((Class<Object>) ref.getClass(), parts[parts.length - 1]).apply(ref, obj);
  }

  /**
   * Get property value. Supports deep hierarchy level (prop1.prop2.prop3).
   * 
   * @param bean The object which hold the property
   * @param name The property name
   * @return the property value
   * @throws IllegalArgumentException if the bean is null or the name is null or the property does
   *         not exist
   */
  public static Object getProperty(Object bean, String name) {
    if (bean == null) {
      throw new IllegalArgumentException(NO_BEAN_SPECIFIED);
    }
    if (name == null) {
      throw new IllegalArgumentException(NO_NAME_SPECIFIED_FOR_BEAN_CLASS + bean.getClass() + "'");
    }

    Object ref = bean;

    for (String part : name.split("\\.")) {
      ref = getProperty0(ref, part);
    }

    return ref;
  }

  @SuppressWarnings("unchecked")
  private static Object getProperty0(Object bean, String name) {
    return ((Function<Object, Object>) getter(bean.getClass(), name)).apply(bean);
  }

  /**
   * Create a property getter function.
   * 
   * @param <T> the bean type
   * @param <S> the bean super type
   * @param bean The class which hold the property
   * @param name The direct property name
   * @return a function (object) to (value)
   * @throws IllegalArgumentException if the bean is null or the name is null or the property does
   *         not exist
   */
  @SuppressWarnings("unchecked")
  public static <T, S extends T> Function<T, Object> getter(final Class<S> bean,
      final String name) {

    if (bean == null) {
      throw new IllegalArgumentException(NO_BEAN_SPECIFIED);
    }
    if (name == null) {
      throw new IllegalArgumentException(NO_NAME_SPECIFIED_FOR_BEAN_CLASS + bean.getClass() + "'");
    }

    String baseName = name.substring(0, 1).toUpperCase() + name.substring(1);

    Method me = getMethod0(bean, "get" + baseName);
    if (me == null) {
      me = getMethod0(bean, "is" + baseName);
    }

    if (me == null) {
      return getterField(bean, name);
    } else {
      return (Function<T, Object>) getterMethod(name, me);
    }
  }

  /**
   * Create a getter function from a method.
   * 
   * @param name The property name for throwing exceptions
   * @param method The method
   * @return a function (object) to (value)
   */
  public static Function<Object, Object> getterMethod(final String name, Method method) {
    return new Function<Object, Object>() {
      @Override
      public Object apply(Object arg0) {
        try {
          return method.invoke(arg0);
        } catch (IllegalAccessException | InvocationTargetException ex) {
          throw new IllegalArgumentException(CANNOT_GET_THE_VALUE_OF_THE_FIELD + name, ex);
        }
      }

    };
  }

  /**
   * Create a getter function from a field of the bean class.
   * 
   * @param <T> the bean type
   * @param <S> the bean super type
   * @param bean The class which hold the property
   * @param name The property name
   * @return a function (object) to (value)
   * @throws IllegalArgumentException the property does not exist
   */
  public static <T, S extends T> Function<T, Object> getterField(final Class<S> bean,
      final String name) {
    final Field f = Reflect.getFieldByName(bean, name);
    if (f == null) {
      throw new IllegalArgumentException(CANNOT_FIND_METHOD_OR_FIELD_FOR_PROPERTY + name);
    } else {
      Reflect.enable(f);
      return new Function<T, Object>() {

        @Override
        public Object apply(T arg0) {
          try {
            return f.get(arg0);
          } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException(CANNOT_GET_THE_VALUE_OF_THE_FIELD + name, ex);
          }
        }

      };
    }
  }

  /**
   * Create a setter function.
   * 
   * @param <T> the bean type
   * @param bean The bean class
   * @param name The property name
   * @return a function (object,value) to (result or null)
   * @throws IllegalArgumentException if the bean is null or the name is null or the property does
   *         not exists
   */
  public static <T> BiFunction<T, Object, Object> setter(final Class<T> bean, final String name) {
    checkSetterParameters(bean, name);

    String baseName = name.substring(0, 1).toUpperCase() + name.substring(1);

    Method method = getMethod1(bean, "set" + baseName, null);

    if (method == null) {
      method = getMethod1(bean, name, null);
    }

    if (method == null) {
      return setterField(bean, name);
    } else {
      Reflect.enable(method);
      final Method refMethod = method;
      return new BiFunction<T, Object, Object>() {

        @Override
        public Object apply(T ref, Object arg) {
          try {
            return refMethod.invoke(ref, arg);
          } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalArgumentException("cannot set the value of the field " + name, ex);
          }
        }
      };
    }
  }

  private static <T> void checkSetterParameters(final Class<T> bean, final String name) {
    if (bean == null) {
      throw new IllegalArgumentException(NO_BEAN_SPECIFIED);
    }
    if (name == null) {
      throw new IllegalArgumentException(NO_NAME_SPECIFIED_FOR_BEAN_CLASS + bean + "'");
    }
  }

  /**
   * Create a setter function from a field of a bean.
   * 
   * @param <T> the bean type
   * @param bean The bean class
   * @param name The property name
   * @return a function (object,value) to (null)
   * @throws IllegalArgumentException if the property does not exists
   */
  public static <T> BiFunction<T, Object, Object> setterField(final Class<T> bean,
      final String name) {
    final Field f = Reflect.getFieldByName(bean, name);
    if (f == null) {
      throw new IllegalArgumentException(CANNOT_FIND_METHOD_OR_FIELD_FOR_PROPERTY + name);
    } else {
      Reflect.enable(f);
      return new BiFunction<T, Object, Object>() {

        @Override
        public Object apply(T ref, Object val) {
          try {
            f.set(ref, val);
          } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("cannot set the value of the field " + name, ex);
          }
          return null;
        }
      };
    }
  }

  private static Method getMethod1(Class<?> bean, String methodName, Class<?> type) {
    for (Method m : bean.getMethods()) {
      if (m.getName().equals(methodName) && m.getParameterTypes().length == 1
          && isFirstParam(m, type)) {
        return m;
      }
    }
    return null;
  }

  private static boolean isFirstParam(Method method, Class<?> type) {
    return type == null || method.getParameterTypes()[0] == type
        || method.getParameterTypes()[0].isAssignableFrom(type);
  }

  private static Method getMethod0(Class<?> clazz, String methodName) {
    for (Method m : clazz.getMethods()) {
      if (m.getName().equals(methodName) && m.getParameterTypes().length == 0) {
        return m;
      }
    }
    return null;
  }
}
