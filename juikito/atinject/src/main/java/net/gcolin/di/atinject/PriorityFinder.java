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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gcolin.common.reflect.Reflect;

/**
 * Extract the priority with the annotation {@code javax.annotation.Priority}.
 * 
 * <p>
 * If the annotation is not in the classpath, the CDI will work.
 * </p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class PriorityFinder {

  private static Class<? extends Annotation> priority;
  private static Method priorityValue;

  static {
    if (Reflect.exists("javax.annotation.Priority", PriorityFinder.class.getClassLoader())) {
      try {
        priority = (Class<? extends Annotation>) PriorityFinder.class.getClassLoader()
            .loadClass("javax.annotation.Priority");
        priorityValue = priority.getMethod("value");
      } catch (ClassNotFoundException | NoSuchMethodException | SecurityException ex) {
        Logger log = LoggerFactory.getLogger("net.gcolin.di.atinject.PriorityFinder");
        log.error("cannot find javax.annotation.Priority");
        log.debug(ex.getMessage(), ex);
      }
    }
  }

  /**
   * The priority of a class.
   * 
   * @param clazz a class
   * @return a priority
   */
  public static int getPriority(Class<?> clazz) {
    if (priority != null) {
      Object priorityAnnotation = clazz.getAnnotation(priority);
      if (priorityAnnotation != null) {
        try {
          return (Integer) priorityValue.invoke(priorityAnnotation);
        } catch (IllegalAccessException | InvocationTargetException ex) {
          LoggerFactory.getLogger("net.gcolin.di.atinject.PriorityFinder").debug(ex.getMessage(), ex);
          return Integer.MAX_VALUE;
        }
      }
    }
    return Integer.MAX_VALUE;
  }

}
