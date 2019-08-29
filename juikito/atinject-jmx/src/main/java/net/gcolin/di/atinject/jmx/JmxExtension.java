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
package net.gcolin.di.atinject.jmx;

import java.lang.management.ManagementFactory;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.core.InjectException;

/**
 * Enable JMX annotations.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JmxExtension implements Extension {

  private Set<ObjectName> names = new HashSet<>();

  @Override
  public void doStarted(Environment environment) {
    for (Class<?> clazz : environment.getBeanClasses()) {
      add(environment, clazz, null);
    }
  }

  public void add(Object instance) {
    add(null, instance.getClass(), instance);
  }

  public void remove(Object instance) {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName oname = new ObjectName(getJmxName(instance.getClass(), instance));
      if (mbs.isRegistered(oname)) {
        mbs.unregisterMBean(oname);
      }
    } catch (MalformedObjectNameException | MBeanRegistrationException
        | InstanceNotFoundException ex) {
      throw new InjectException(ex);
    }
  }

  @SuppressWarnings("unchecked")
  private void add(Environment environment, Class<?> clazz, Object instance) {
    List<Method> methods = Reflect.find(clazz, Reflect.METHOD_ITERATOR,
        m -> m.isAnnotationPresent(Jmx.class), Reflect.flatList());
    List<Field> fields = Reflect.find(clazz, Reflect.FIELD_ITERATOR,
        m -> m.isAnnotationPresent(Jmx.class), Reflect.flatList());

    if (methods == null) {
      methods = Collections.emptyList();
    }

    if (fields == null) {
      fields = Collections.emptyList();
    }

    if (!fields.isEmpty() || !methods.isEmpty()) {

      ReflectMBean bean;

      if (instance != null) {
        bean = new ReflectMBean(clazz, methods, fields, () -> instance);
      } else {
        bean = new ReflectMBean(clazz, methods, fields,
            (Provider<Object>) environment.getProvider(clazz));
      }

      try {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName oname = new ObjectName(getJmxName(clazz, instance));
        if (!mbs.isRegistered(oname)) {
          names.add(oname);
          mbs.registerMBean(bean, oname);
        }
      } catch (MalformedObjectNameException | InstanceAlreadyExistsException
          | MBeanRegistrationException | NotCompliantMBeanException ex) {
        throw new InjectException(ex);
      }
    }
  }

  @Override
  public void doStopped(Environment environment) {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    for (ObjectName name : names) {
      if (mbs.isRegistered(name)) {
        try {
          mbs.unregisterMBean(name);
        } catch (MBeanRegistrationException | InstanceNotFoundException ex) {
          throw new InjectException(ex);
        }
      }
    }
    names.clear();
  }

  private String getJmxName(Class<?> clazz, Object instance) {
    StringBuilder objname = new StringBuilder();
    String name = clazz.getName();
    name = name.substring(0, name.length() - clazz.getSimpleName().length() - 1);
    objname.append(name).append(":type=").append(clazz.getSimpleName());
    if (instance != null) {
      List<AccessibleObject> fieldAttrs = new ArrayList<>();
      List<Field> fieldList = Reflect.find(clazz, Reflect.FIELD_ITERATOR,
          m -> m.isAnnotationPresent(JmxAttribute.class), Reflect.flatList());
      if(fieldList != null) {
        fieldAttrs.addAll(fieldList);
      }
      List<Method> methodList = Reflect.find(clazz, Reflect.METHOD_ITERATOR,
          m -> m.isAnnotationPresent(JmxAttribute.class) && Reflect.isGetter(m),
          Reflect.flatList());
      if(methodList != null) {
        fieldAttrs.addAll(methodList);
      }

      if (!fieldAttrs.isEmpty()) {
        Collections.sort(fieldAttrs, (a, b) -> {
          JmxAttribute aa = a.getAnnotation(JmxAttribute.class);
          JmxAttribute ba = b.getAnnotation(JmxAttribute.class);
          if (aa.sort() > ba.sort()) {
            return 1;
          } else if (aa.sort() < ba.sort()) {
            return -1;
          } else {
            return 0;
          }
        });
        for (AccessibleObject aa : fieldAttrs) {
          JmxAttribute a = aa.getAnnotation(JmxAttribute.class);
          String aname = a.value();
          if (aname.isEmpty()) {
            aname =
                aa instanceof Field ? ((Field) aa).getName() : Reflect.getPropertyName((Method) aa);
          }
          Reflect.enable(aa);
          String value;
          if (aa instanceof Field) {
            try {
              value = String.valueOf(((Field) aa).get(instance));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
              throw new InjectException(ex);
            }
          } else {
            try {
              value = String.valueOf(((Method) aa).invoke(instance));
            } catch (IllegalArgumentException | IllegalAccessException
                | InvocationTargetException ex) {
              throw new InjectException(ex);
            }
          }

          objname.append(",").append(aname).append("=").append(value);
        }
      }
    }
    return objname.toString();
  }

}
