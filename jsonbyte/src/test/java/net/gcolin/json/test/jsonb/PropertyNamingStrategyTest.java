/*******************************************************************************
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0 and
 * Eclipse Distribution License v. 1.0 which accompanies this distribution. The Eclipse Public
 * License is available at http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution
 * License is available at http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors: Roman Grigoriadi
 ******************************************************************************/

package net.gcolin.json.test.jsonb;

import static org.junit.Assert.assertEquals;

import net.gcolin.common.lang.Strings;
import net.gcolin.jsonb.build.LowerCaseWithDashesStrategy;
import net.gcolin.jsonb.build.LowerCaseWithUnderscoresStrategy;
import net.gcolin.jsonb.build.UpperCamelCaseWithSpacesStrategy;

import org.junit.Test;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyNamingStrategy;

/**
 * Tests naming strategies.
 *
 * @author Roman Grigoriadi
 */
public class PropertyNamingStrategyTest {

  public static class NamingPojo {

    public NamingPojo() {}

    public NamingPojo(String upperCasedProperty, String _startingWithUnderscoreProperty,
        String CAPS_UNDERSCORE_PROPERTY) {
      this.upperCasedProperty = upperCasedProperty;
      this._startingWithUnderscoreProperty = _startingWithUnderscoreProperty;
      this.CAPS_UNDERSCORE_PROPERTY = CAPS_UNDERSCORE_PROPERTY;
    }

    public String upperCasedProperty;
    public String _startingWithUnderscoreProperty;
    public String CAPS_UNDERSCORE_PROPERTY;
  }

  private final NamingPojo pojo = new NamingPojo("abc", "def", "ghi");

  @Test
  public void testLowerCase() throws Exception {
    PropertyNamingStrategy strategy = new LowerCaseWithUnderscoresStrategy();
    assertEquals("camel_case_property", strategy.translateName("camelCaseProperty"));
    assertEquals("camelcase_property", strategy.translateName("CamelcaseProperty"));
    assertEquals("camel_case_property", strategy.translateName("CamelCaseProperty"));
    assertEquals("_camel_case_property", strategy.translateName("_camelCaseProperty"));
    assertEquals("_camel_case_property", strategy.translateName("_CamelCaseProperty"));

    Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
        .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_UNDERSCORES));
    String lowercaseUnderscoresJson =
        "{\"upper_cased_property\":\"abc\",\"_starting_with_underscore_property\":\"def\",\"caps_underscore_property\":\"ghi\"}";
    assertEquals(lowercaseUnderscoresJson, jsonb.toJson(pojo));
    NamingPojo result = jsonb.fromJson(lowercaseUnderscoresJson, NamingPojo.class);
    assertResult(result);

  }

  @Test
  public void testLowerDashes() throws Exception {
    PropertyNamingStrategy strategy = new LowerCaseWithDashesStrategy();
    assertEquals("camel-case-property", strategy.translateName("camelCaseProperty"));
    assertEquals("camelcase-property", strategy.translateName("CamelcaseProperty"));
    assertEquals("camel-case-property", strategy.translateName("CamelCaseProperty"));
    assertEquals("-camel-case-property", strategy.translateName("-camelCaseProperty"));
    assertEquals("-camel-case-property", strategy.translateName("-CamelCaseProperty"));

    Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
        .withPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE_WITH_DASHES));
    String lowercaseDashesJson =
        "{\"upper-cased-property\":\"abc\",\"_starting-with-underscore-property\":\"def\",\"caps_underscore_property\":\"ghi\"}";
    assertEquals(lowercaseDashesJson, jsonb.toJson(pojo));
    NamingPojo result = jsonb.fromJson(lowercaseDashesJson, NamingPojo.class);
    assertResult(result);
  }

  @Test
  public void testUpperCase() {
    PropertyNamingStrategy upperCaseStrat = x -> Strings.capitalize(x);
    assertEquals("UpperCamelCase", upperCaseStrat.translateName("upperCamelCase"));
    assertEquals("UpperCamelCase", upperCaseStrat.translateName("UpperCamelCase"));

    Jsonb jsonb = JsonbBuilder.create(
        new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE));
    String upperCased =
        "{\"UpperCasedProperty\":\"abc\",\"_startingWithUnderscoreProperty\":\"def\",\"CAPS_UNDERSCORE_PROPERTY\":\"ghi\"}";
    assertEquals(upperCased, jsonb.toJson(pojo));
    NamingPojo result = jsonb.fromJson(upperCased, NamingPojo.class);
    assertResult(result);
  }

  @Test
  public void testUpperCaseWithSpaces() {
    PropertyNamingStrategy upperCaseWithSpacesStrat = new UpperCamelCaseWithSpacesStrategy();
    assertEquals("Upper Camel Case", upperCaseWithSpacesStrat.translateName("upperCamelCase"));
    assertEquals("Upper Camel Case", upperCaseWithSpacesStrat.translateName("UpperCamelCase"));

    Jsonb jsonb = JsonbBuilder.create(new JsonbConfig()
        .withPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE_WITH_SPACES));
    String upperCased =
        "{\"Upper Cased Property\":\"abc\",\"_starting With Underscore Property\":\"def\",\"CAPS_UNDERSCORE_PROPERTY\":\"ghi\"}";
    assertEquals(upperCased, jsonb.toJson(pojo));
    NamingPojo result = jsonb.fromJson(upperCased, NamingPojo.class);
    assertResult(result);
  }

  @Test
  public void testCaseInsensitive() {

    Jsonb jsonb = JsonbBuilder.create(
        new JsonbConfig().withPropertyNamingStrategy(PropertyNamingStrategy.CASE_INSENSITIVE));
    String upperCased =
        "{\"upperCasedProperty\":\"abc\",\"_startingWithUnderscoreProperty\":\"def\",\"CAPS_UNDERSCORE_PROPERTY\":\"ghi\"}";
    assertEquals(upperCased, jsonb.toJson(pojo));
    NamingPojo result = jsonb.fromJson(
        "{\"caPS_unDERscore_prOPERty\":\"ghi\",\"_startingwithUndERSCorePrOPERTy\":\"def\",\"upPERCASedProPerty\":\"abc\"}",
        NamingPojo.class);
    assertResult(result);
  }

  @Test
  public void testCustom() {
    Jsonb jsonb = JsonbBuilder
        .create(new JsonbConfig().withPropertyNamingStrategy(new PropertyNamingStrategy() {
          @Override
          public String translateName(String propertyName) {
            return propertyName + "_" + propertyName.toUpperCase();
          }
        }));

    String custom =
        "{\"upperCasedProperty_UPPERCASEDPROPERTY\":\"abc\",\"_startingWithUnderscoreProperty__STARTINGWITHUNDERSCOREPROPERTY\":\"def\",\"CAPS_UNDERSCORE_PROPERTY_CAPS_UNDERSCORE_PROPERTY\":\"ghi\"}";
    assertEquals(custom, jsonb.toJson(pojo));
    NamingPojo result = jsonb.fromJson(custom, NamingPojo.class);
    assertResult(result);
  }

  private void assertResult(NamingPojo result) {
    assertEquals("abc", result.upperCasedProperty);
    assertEquals("def", result._startingWithUnderscoreProperty);
    assertEquals("ghi", result.CAPS_UNDERSCORE_PROPERTY);
  }


}
