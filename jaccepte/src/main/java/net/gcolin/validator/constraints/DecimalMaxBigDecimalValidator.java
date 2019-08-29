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
import javax.validation.constraints.DecimalMax;

/**
 * A ConstraintValidator DecimalMax for a BigDecimal.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DecimalMaxBigDecimalValidator implements ConstraintValidator<DecimalMax, BigDecimal> {

  private BigDecimal max;

  @Override
  public void initialize(DecimalMax constraintAnnotation) {
    max = new BigDecimal(constraintAnnotation.value());
  }

  @Override
  public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
    return value == null || value.compareTo(max) <= 0;
  }

}
