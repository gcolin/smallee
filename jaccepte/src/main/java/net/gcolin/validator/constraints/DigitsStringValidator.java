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

package net.gcolin.validator.constraints;

import java.math.BigDecimal;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Digits;

/**
 * A ConstraintValidator Digits for a String.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DigitsStringValidator implements ConstraintValidator<Digits, String> {

  private int faction;
  private int integer;

  @Override
  public void initialize(Digits constraintAnnotation) {
    faction = constraintAnnotation.fraction();
    integer = constraintAnnotation.integer();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    BigDecimal bigNum = new BigDecimal(value);
    int scale = bigNum.scale();
    int integerPartLength = bigNum.precision() - scale;
    int fractionPartLength = scale < 0 ? 0 : scale;
    return integer >= integerPartLength && faction >= fractionPartLength;
  }

}
