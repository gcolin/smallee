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

package net.gcolin.validation.test;

import net.gcolin.common.reflect.Reflect;

import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ParameterNameProvider;
import javax.validation.Path.Node;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

/**
 * Test parameter names.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ParameterNameTest {

  public static class Obj {

    public void action(@NotNull @Name("value") String value) {

    }

  }

  public static class ParameterNameProviderImpl implements ParameterNameProvider {

    private ParameterNameProvider delegate;

    public ParameterNameProviderImpl(ParameterNameProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public List<String> getParameterNames(Constructor<?> cons) {
      return delegate.getParameterNames(cons);
    }

    @Override
    public List<String> getParameterNames(Method meth) {
      Annotation[][] aa = meth.getParameterAnnotations();
      List<String> defaultNames = delegate.getParameterNames(meth);
      for (int i = 0, l = aa.length; i < l; i++) {
        Annotation[] an = aa[i];
        Name name = Reflect.getAnnotation(an, Name.class);
        if (name != null) {
          defaultNames.set(i, name.value());
          break;
        }
      }
      return defaultNames;
    }
  }

  @Test
  public void test() throws Exception {
    Validator validator =
        Validation.buildDefaultValidatorFactory().usingContext()
            .parameterNameProvider(new ParameterNameProviderImpl(
                Validation.buildDefaultValidatorFactory().getParameterNameProvider()))
            .getValidator();
    Set<ConstraintViolation<Obj>> violations =
        validator.forExecutables().validateParameters(new Obj(),
            Obj.class.getMethod("action", String.class), new Object[] {null});
    Assert.assertEquals(1, violations.size());
    ConstraintViolation<Obj> cv = violations.iterator().next();
    Iterator<Node> it = cv.getPropertyPath().iterator();
    Assert.assertEquals("action", it.next().getName());
    Assert.assertEquals("value", it.next().getName());
  }

}
