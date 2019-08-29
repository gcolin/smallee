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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import net.gcolin.validation.test.ValidateClassTest.Person;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

/**
 * A big test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Constraint(
    validatedBy = SecurityCheck.SecurityCheckValidator.class)
@Target({TYPE})
@Retention(RUNTIME)
public @interface SecurityCheck {

  /**
   * The message.
   * 
   * @return the message
   */
  public abstract String message() default "Security check failed.";

  /**
   * The groups.
   * 
   * @return the groups
   */
  public abstract Class<?>[] groups() default {};

  /**
   * The payload.
   * 
   * @return the payload
   */
  public abstract Class<? extends Payload>[] payload() default {};

  public class SecurityCheckValidator implements ConstraintValidator<SecurityCheck, Object> {


    public void initialize(SecurityCheck parameters) {

    }

    /**
     * Check if valid.
     */
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
      if (object == null) {
        return true;
      }

      if (!(object instanceof Person)) {
        return false;
      }

      return ((Person) object).name != null;
    }
  }
}
