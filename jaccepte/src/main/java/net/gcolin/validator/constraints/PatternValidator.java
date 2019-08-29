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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Pattern;

/**
 * A ConstraintValidator Pattern for a CharSequence.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class PatternValidator implements ConstraintValidator<Pattern, CharSequence> {

  java.util.regex.Pattern pattern;

  @Override
  public void initialize(Pattern pa) {
    pattern = java.util.regex.Pattern.compile(pa.regexp());
  }

  @Override
  public boolean isValid(CharSequence cs,
      ConstraintValidatorContext context) {
    return cs == null || pattern.matcher(cs).matches();
  }

}
