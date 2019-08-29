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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.inject.Provider;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import net.gcolin.common.reflect.Reflect;

/**
 * The MBean implementation that reads annotations.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ReflectMBean implements DynamicMBean {

  private Map<String, Supplier<Object>> attributesGetter = new HashMap<>();
  private Map<String, Consumer<Object>> attributesSetter = new HashMap<>();
  private Map<String, Function<Object[], Object>> functions = new HashMap<>();
  private MBeanInfo info;

  public ReflectMBean(Class<?> clazz, List<Method> methods, List<Field> fields,
      Provider<Object> provider) {
    Map<String, Item> mattributes = new HashMap<>();
    List<MBeanOperationInfo> operations = new ArrayList<>();
    for (int i = 0; i < methods.size(); i++) {
      Method method = methods.get(i);
      Jmx jmx = method.getAnnotation(Jmx.class);
      String name = jmx.value().isEmpty() ? method.getName() : jmx.value();
      if (Reflect.isGetter(method)) {
        if (jmx.value().isEmpty()) {
          name = Reflect.getPropertyName(method);
        }
        Item item = mattributes.get(name);
        if (item == null) {
          item = new Item();
          mattributes.put(name, item);
          item.type = method.getReturnType();
        }
        item.getter = () -> {
          try {
            return method.invoke(provider.get());
          } catch (IllegalAccessException | IllegalArgumentException
              | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
          }
        };
        item.updateDescription(jmx);
        continue;
      }
      if (Reflect.isSetter(method)) {
        if (jmx.value().isEmpty()) {
          name = Reflect.getPropertyName(method);
        }
        Item item = mattributes.get(name);
        if (item == null) {
          item = new Item();
          mattributes.put(name, item);
          item.type = method.getParameterTypes()[0];
        }
        item.setter = val -> {
          try {
            method.invoke(provider.get(), val);
          } catch (IllegalAccessException | IllegalArgumentException
              | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
          }
        };
        item.updateDescription(jmx);
        continue;
      }

      Reflect.enable(method);
      String[] signature = new String[method.getParameterCount()];
      MBeanParameterInfo[] sign = new MBeanParameterInfo[signature.length];
      for (int j = 0; j < signature.length; j++) {
        signature[j] = method.getParameterTypes()[j].getName();
        Jmx jmxParam = Reflect.getAnnotation(method.getParameterAnnotations()[j], Jmx.class);
        sign[j] = new MBeanParameterInfo(
            jmxParam == null || jmxParam.value().isEmpty() ? "arg" + j : jmxParam.value(),
            signature[j], jmxParam == null ? "" : jmxParam.description());
      }

      functions.put(buildKey(name, signature), args -> {
        try {
          return method.invoke(provider.get(), args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
          throw new IllegalStateException(ex);
        }
      });
      operations.add(new MBeanOperationInfo(name, jmx.description(), sign,
          method.getReturnType().getName(), MBeanOperationInfo.UNKNOWN));
    }
    
    for (int i = 0; i < fields.size(); i++) {
      Field field = fields.get(i);
      Jmx jmx = field.getAnnotation(Jmx.class);
      String name = jmx.value().isEmpty() ? field.getName() : jmx.value();
      Item item = mattributes.get(name);
      if (item == null) {
        item = new Item();
        mattributes.put(name, item);
        item.type = field.getType();
      }
      
      if(item.setter == null && !Modifier.isFinal(field.getModifiers())) {
        Reflect.enable(field);
        item.setter = val -> {
          try {
            field.set(provider.get(), val);
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
          }
        };
      }
      
      if(item.getter == null) {
        Reflect.enable(field);
        item.getter = () -> {
          try {
            return field.get(provider.get());
          } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new IllegalStateException(ex);
          }
        };
      }
      
      item.updateDescription(jmx);
    }
    
    List<MBeanAttributeInfo> attributes = new ArrayList<>();
    for(Entry<String, Item> attr : mattributes.entrySet()) {
      boolean readeable = attr.getValue().getter != null;
      boolean writable = attr.getValue().setter != null;
      attributes.add(new MBeanAttributeInfo(attr.getKey(), attr.getValue().type.getName(), attr.getValue().description,
          readeable, writable, false));
      if(readeable) {
        attributesGetter.put(attr.getKey(), attr.getValue().getter);
      }
      if(writable) {
        attributesSetter.put(attr.getKey(), attr.getValue().setter);
      }
    }
    
    info = new MBeanInfo(clazz.getName(), clazz.getName(),
        attributes.toArray(new MBeanAttributeInfo[attributes.size()]), new MBeanConstructorInfo[0],
        operations.toArray(new MBeanOperationInfo[operations.size()]),
        new MBeanNotificationInfo[0]);
  }

  @Override
  public Object getAttribute(String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException {
    Supplier<Object> s = attributesGetter.get(attribute);
    if (s == null) {
      throw new AttributeNotFoundException();
    }
    return s.get();
  }

  @Override
  public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
      InvalidAttributeValueException, MBeanException, ReflectionException {
    Consumer<Object> c = attributesSetter.get(attribute.getName());
    if (c == null) {
      throw new AttributeNotFoundException();
    }
    c.accept(attribute.getValue());
  }

  @Override
  public AttributeList getAttributes(String[] attributes) {
    AttributeList list = new AttributeList(attributes.length);
    for (String attribute : attributes) {
      Supplier<Object> s = this.attributesGetter.get(attribute);
      if (s != null) {
        list.add(new Attribute(attribute, s.get()));
      }
    }
    return list;
  }

  @Override
  public AttributeList setAttributes(AttributeList attributes) {
    for (Object attribute : attributes) {
      Attribute attr = (Attribute) attribute;
      Consumer<Object> c = attributesSetter.get(attr.getName());
      if (c != null) {
        c.accept(attr.getValue());
      }
    }
    return attributes;
  }

  private String buildKey(String actionName, String[] signature) {
    if (signature == null || signature.length == 0) {
      return actionName;
    }
    StringBuilder str = new StringBuilder(actionName);
    for (String part : signature) {
      str.append('@').append(part);
    }
    return str.toString();
  }

  @Override
  public Object invoke(String actionName, Object[] params, String[] signature)
      throws MBeanException, ReflectionException {
    try {
      return functions.get(buildKey(actionName, signature)).apply(params);
    } catch (Exception ex) {
      throw new ReflectionException(ex);
    }
  }

  @Override
  public MBeanInfo getMBeanInfo() {
    return info;
  }
  
  private static class Item {
    
    Supplier<Object> getter;
    Consumer<Object> setter;
    Class<?> type;
    String description;
    
    void updateDescription(Jmx jmx) {
      if(description == null && !jmx.description().isEmpty()) {
        description = jmx.description();
      }
    }
    
    
  }

}
