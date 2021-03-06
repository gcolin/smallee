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

package net.gcolin.jsonb.build;

import javax.json.bind.config.PropertyNamingStrategy;

/**
 * Abstract class for some PropertyNamingStrategy.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class LowerCaseStrategy implements PropertyNamingStrategy {

  @Override
  public String translateName(String propertyName) {
    char last = Character.MIN_VALUE;
    StringBuilder str = null;
    int prec = 0;
    for (int i = 0; i < propertyName.length(); i++) {
      char current = propertyName.charAt(i);
      if (Character.isUpperCase(current)) {
        if (str == null) {
          str = new StringBuilder();
        }
        str.append(propertyName, prec, i);
        prec = i + 1;
        if (i > 0 && isLowerCaseCharacter(last)) {
          str.append(getSerarator());
        }
        str.append(Character.toLowerCase(current));
      }
      last = current;
    }

    if (str != null) {
      str.append(propertyName, prec, propertyName.length());
      return str.toString();
    }
    return propertyName;
  }

  public static boolean isLowerCaseCharacter(char character) {
    return Character.isAlphabetic(character) && Character.isLowerCase(character);
  }

  protected abstract char getSerarator();

}
