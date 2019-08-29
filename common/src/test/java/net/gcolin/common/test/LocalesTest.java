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

package net.gcolin.common.test;

import net.gcolin.common.lang.Locales;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class LocalesTest {

  @Test
  public void testNull() {
    Assert.assertNull(Locales.fromString(null));
    Assert.assertNull(Locales.fromString(""));
  }

  @Test
  public void testLang() {
    Assert.assertEquals(Locale.FRENCH, Locales.fromString("fr"));
    Assert.assertEquals(Locale.ENGLISH, Locales.fromString("en"));
  }

  @Test
  public void testCountry() {
    Assert.assertEquals(Locale.FRANCE, Locales.fromString("fr_FR"));
    Assert.assertEquals(Locale.CANADA, Locales.fromString("en_CA"));

    Assert.assertEquals(Locale.FRANCE, Locales.fromString("fr-FR"));
    Assert.assertEquals(Locale.CANADA, Locales.fromString("en-CA"));
  }

  @Test
  public void testRegion() {
    Assert.assertEquals(new Locale("fr", "FR", "br"), Locales.fromString("fr_FR_br"));
    Assert.assertEquals(new Locale("fr", "FR", "br"), Locales.fromString("fr-FR-br"));
  }

  @Test
  public void testInvalidSep() {
    try {
      Locales.fromString("fr,FR");
      Assert.fail("invalid sep");
    } catch (IllegalArgumentException ex) {
      // ok
    }

    try {
      Locales.fromString("fr-FR,br");
      Assert.fail("invalid sep");
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void testInvalidlang() {
    try {
      Locales.fromString("f0");
      Assert.fail("invalid lang");
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void testInvalidCountry() {
    try {
      Locales.fromString("fr_fr");
      Assert.fail("invalid country");
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void testInvalidSize() {
    try {
      Locales.fromString("f");
      Assert.fail("invalid size");
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

}
