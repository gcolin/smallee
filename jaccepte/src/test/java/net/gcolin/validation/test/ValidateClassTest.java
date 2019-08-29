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

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class ValidateClassTest {

  @SecurityCheck
  public static class Person implements Human {
    String name;
  }

  @SecurityCheck
  public static class Citizen extends Person {
  }

  @SecurityCheck
  public static interface Human {
  }

  public static class Car {

    @NotNull
    String model;

    @Valid
    public Car(String str) {}

    public Car() {}

  }

  @Test
  public void testContructorValid() throws Exception {
    Validator val = Validation.buildDefaultValidatorFactory().getValidator();
    Constructor<?> constructor = Car.class.getConstructor(String.class);
    Set<ConstraintViolation<Object>> constraintViolations =
        val.forExecutables().validateConstructorReturnValue(constructor, new Car());
    Assert.assertEquals(1, constraintViolations.size());

  }

  @Test
  public void testfail() {
    Set<ConstraintViolation<Person>> violations =
        Validation.buildDefaultValidatorFactory().getValidator().validate(new Citizen());

    Assert.assertEquals(3, violations.size());

    Assert.assertEquals("Security check failed.", violations.iterator().next().getMessage());
  }

}
