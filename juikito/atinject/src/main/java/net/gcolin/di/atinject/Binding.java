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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.inject.Named;

/**
 * Factory for creating bindings manually.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Binding {

  private Class<?> clazz;
  private Environment env;
  private List<Annotation> qualifiers = new ArrayList<>();

  public Binding(Class<?> clazz, Environment env) {
    this.clazz = clazz;
    this.env = env;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Binding named(final String str) {
    Class<?> c = null;
    try {
      c = this.getClass().getClassLoader().loadClass(str);
    } catch (ClassNotFoundException e) {
      env.getLog().log(Level.FINE, e.getMessage(), e);
    }
    if (c != null && c.isAnnotation()) {
      qualifiers.add(new AnnotationWrapper((Class) c));
      return this;
    }
    qualifiers.add(createNamed(str));
    return this;
  }

  public void implementedBy(Class<?> implementation) {
    Annotation[] qualifiersArray = null;
    if (!this.qualifiers.isEmpty()) {
      qualifiersArray = new Annotation[this.qualifiers.size()];
      this.qualifiers.toArray(qualifiersArray);
    } else {
      qualifiersArray = new Annotation[0];
    }
    env.getBinding().put(env.createBindingKey(env.createKey(clazz, clazz, qualifiersArray)), implementation);
  }

  public static class AnnotationWrapper implements Annotation {
    private Class<? extends Annotation> type;

    public AnnotationWrapper(Class<? extends Annotation> type) {
      this.type = type;
    }

    @Override
    public String toString() {
      return "@" + type.getName() + "()";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return type;
    }
  }

  public static Annotation createNamed(final String name) {
    return new Named() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Named.class;
      }

      @Override
      public String value() {
        return name;
      }

      @Override
      public String toString() {
        return "@" + Named.class.getName() + "(value=" + name + ")";
      }

    };
  }

}
