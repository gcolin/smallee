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

package net.gcolin.di.atinject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.inject.Inject;

import net.gcolin.common.reflect.Reflect;

/**
 * Some utility methods.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Reflects {

  private static final Function<Method, String> METHOD_HASH_ACCEPT = m -> {
    StringBuilder str = new StringBuilder();
    if (!Modifier.isPublic(m.getModifiers()) && !Modifier.isProtected(m.getModifiers())) {
      String n = m.getDeclaringClass().getName();
      str.append(n.substring(0, n.lastIndexOf('.'))).append(":");
    }
    str.append(m.getName());
    str.append(m.getReturnType());
    Class<?>[] array = m.getParameterTypes();
    for (int i = 0, l = array.length; i < l; i++) {
      str.append(";").append(array[i]);
    }
    return str.toString();
  };
  private static final Predicate<Method> METHOD_HASH_ADD = m -> Modifier.isStatic(m.getModifiers())
      || Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers());

  private static final Predicate<Field> FIELD_INJECT_STATIC_ACCEPT =
      f -> f.isAnnotationPresent(Inject.class) && Modifier.isStatic(f.getModifiers());

  private static final Predicate<Method> METHOD_INJECT_STATIC_ACCEPT =
      m -> m.isAnnotationPresent(Inject.class) && Modifier.isStatic(m.getModifiers());

  private static final Predicate<Method> METHOD_POSTCONSTRUCT_ACCEPT =
      m -> Reflect.hasAnnotation(m.getAnnotations(), "javax.annotation.PostConstruct")
          && m.getParameterTypes().length == 0;

  private static final Predicate<Method> METHOD_PREDESTROY_ACCEPT =
      m -> Reflect.hasAnnotation(m.getAnnotations(), "javax.annotation.PreDestroy")
          && m.getParameterTypes().length == 0;

  private Reflects() {}

  public static Field[][] findStaticFields(Class<?> clazz) {
    return Reflect.find(clazz, Reflect.FIELD_ITERATOR, FIELD_INJECT_STATIC_ACCEPT,
        Reflect.array(Field.class));
  }

  public static InjectionPoint[] findInjectPoints(Class<?> clazz, Environment env) {
    InjectionPointBuilder[] builders = env.getInjectionPointBuilders();
    List<InjectionPoint> all = new ArrayList<>();
    InjectionPointBuilder builder;
    if (builders.length == 1) {
      builder = builders[0];
    } else {
      builder = new InjectionPointBuilder() {

        @Override
        public InjectionPoint create(Field field, Environment env) {
          for (int i = 0; i < builders.length; i++) {
            InjectionPoint ip = builders[i].create(field, env);
            if (ip != null) {
              return ip;
            }
          }
          return null;
        }

        @Override
        public InjectionPoint create(Method method, Environment env) {
          for (int i = 0; i < builders.length; i++) {
            InjectionPoint ip = builders[i].create(method, env);
            if (ip != null) {
              return ip;
            }
          }
          return null;
        }

      };
    }

    Class<?> cl = clazz;
    Set<String> upper = new HashSet<>();
    Set<String> allmethods = new HashSet<>();
    while (cl != Object.class && cl != null) {
      Method[] methods = cl.getDeclaredMethods();
      for (int i = methods.length - 1; i >= 0; i--) {
        Method method = methods[i];
        String hash = METHOD_HASH_ACCEPT.apply(method);
        if (!upper.contains(hash)) {
          boolean add = METHOD_HASH_ADD.test(method);
          if (add) {
            upper.add(hash);
          }
          if (!allmethods.contains(hash)) {
            InjectionPoint ip = builder.create(method, env);
            if (ip != null) {
              all.add(ip);
            }
          }
          if (!add) {
            allmethods.add(hash);
          }
        }
      }
      Field[] fields = cl.getDeclaredFields();
      for (int i = fields.length - 1; i >= 0; i--) {
        InjectionPoint ip = builder.create(fields[i], env);
        if (ip != null) {
          all.add(ip);
        }
      }
      cl = cl.getSuperclass();
    }
    if (all.isEmpty()) {
      return null;
    }
    InjectionPoint[] ips = new InjectionPoint[all.size()];
    for (int i = 0, j = ips.length - 1; j >= 0; i++, j--) {
      ips[i] = all.get(j);
    }
    return ips;
  }

  public static Method[][] findStaticMethods(Class<?> clazz) {
    return Reflect.find(clazz, Reflect.METHOD_ITERATOR, METHOD_INJECT_STATIC_ACCEPT,
        Reflect.array(Method.class));
  }

  public static Method[][] findPredestroyMethods(Class<?> clazz) {
    return Reflect.find(clazz, Reflect.METHOD_ITERATOR, METHOD_PREDESTROY_ACCEPT,
        Reflect.array(Method.class));
  }

  public static Method[][] findPostconstructMethods(Class<?> clazz) {
    return Reflect.find(clazz, Reflect.METHOD_ITERATOR, METHOD_POSTCONSTRUCT_ACCEPT,
        Reflect.array(Method.class));
  }

  public static Class<?> getInterface(Class<?> c) {
    for (Class<?> i : c.getInterfaces()) {
      if (i.getSimpleName().equals("I" + c.getSimpleName())) {
        return i;
      }
    }
    return c;
  }

  public static boolean hasAnnotation(Annotation[] array,
      Set<Class<? extends Annotation>> possiblities) {
    for (int i = 0; i < array.length; i++) {
      if(possiblities.contains(array[i].annotationType())) {
        return true;
      }
    }
    return false;
  }
  
  @SuppressWarnings("unchecked")
  public static <T extends Annotation> T getAnnotation(Annotation[] array,
      Set<Class<? extends Annotation>> possiblities) {
    for (int i = 0; i < array.length; i++) {
      if(possiblities.contains(array[i].annotationType())) {
        return (T) array[i];
      }
    }
    return null;
  }
}
