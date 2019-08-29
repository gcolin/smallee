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

package net.gcolin.common.lang;

import java.util.Locale;

/**
 * The {@code Locales} converts a String to a Locale in the UTC time zone.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see Locale
 */
public class Locales {

  private static final String INVALID_LOCALE_FORMAT = "Invalid locale format: ";
  private static final int REGION_LOC_SIZE = 8;
  private static final int COUNTRY_LOC_SIZE = 5;
  private static final int LANG_LOC_SIZE = 2;

  private Locales() {}

  /**
   * Convert a string to Locale.
   * 
   * @param locale a string
   * @return a locale
   */
  public static Locale fromString(String locale) {
    if (!Strings.isNullOrEmpty(locale)) {
      String str = locale;
      int len = str.length();
      if (len != LANG_LOC_SIZE && len != COUNTRY_LOC_SIZE && len != REGION_LOC_SIZE) {
        validateSize(str, len);
        str = str.substring(0, LANG_LOC_SIZE);
        len = LANG_LOC_SIZE;
      }
      validateLang(str);
      if (len == LANG_LOC_SIZE) {
        return new Locale(str, "");
      } else {
        return getCountryLocale(str, len);
      }
    } else {
      return null;
    }
  }

  private static void validateSize(String str, int len) {
    if (len < 2) {
      throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
    }
  }

  private static Locale getCountryLocale(String str, int len) {
    validateSep(str, LANG_LOC_SIZE);
    char ch3 = str.charAt(LANG_LOC_SIZE + 1);
    if (ch3 == '_' || ch3 == '-') {
      return new Locale(str.substring(0, LANG_LOC_SIZE), "", str.substring(COUNTRY_LOC_SIZE - 1));
    }
    char ch4 = str.charAt(COUNTRY_LOC_SIZE - 1);
    validateCountry(str, ch3, ch4);
    if (len == COUNTRY_LOC_SIZE) {
      return new Locale(str.substring(0, LANG_LOC_SIZE), str.substring(LANG_LOC_SIZE + 1));
    } else {
      validateSep(str, COUNTRY_LOC_SIZE);
      return new Locale(str.substring(0, LANG_LOC_SIZE),
          str.substring(LANG_LOC_SIZE + 1, COUNTRY_LOC_SIZE), str.substring(REGION_LOC_SIZE - 2));
    }
  }

  private static void validateSep(String str, int index) {
    char ch = str.charAt(index);
    if (ch != '_' && ch != '-') {
      throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
    }
  }

  private static void validateCountry(String str, char ch3, char ch4) {
    if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
      throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
    }
  }

  private static void validateLang(String str) {
    char ch0 = str.charAt(0);
    char ch1 = str.charAt(1);
    if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
      throw new IllegalArgumentException(INVALID_LOCALE_FORMAT + str);
    }
  }
}
