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

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

/**
 * A ConstraintValidatorFactory implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ConstraintValidatorFactoryImpl implements ConstraintValidatorFactory {

  private Map<Class<?>, ConstraintValidator<?, ?>> cache = new ConcurrentHashMap<>();

  @Override
  public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
    if (StatelessConstraintValidator.class.isAssignableFrom(key)) {
      @SuppressWarnings("unchecked")
      T value = (T) cache.get(key);
      if (value == null) {
        value = Reflect.newInstance(key);
        cache.put(key, value);
      }
      return value;
    } else {
      return Reflect.newInstance(key);
    }
  }

  @Override
  public void releaseInstance(ConstraintValidator<?, ?> instance) {
    if (instance == null) {
      cache.clear();
      return;
    }
    Object key = null;
    for (Entry<Class<?>, ConstraintValidator<?, ?>> e : cache.entrySet()) {
      if (e.getValue() == instance) {
        key = e.getKey();
      }
    }
    if (key != null) {
      cache.remove(key);
    }
  }

}
