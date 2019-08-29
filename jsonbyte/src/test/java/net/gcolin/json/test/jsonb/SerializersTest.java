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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.junit.Test;

import net.gcolin.json.test.jsonb.model.AnnotatedWithSerializerType;
import net.gcolin.json.test.jsonb.model.Box;
import net.gcolin.json.test.jsonb.model.BoxWithAnnotations;
import net.gcolin.json.test.jsonb.model.Crate;
import net.gcolin.json.test.jsonb.model.CrateDeserializer;
import net.gcolin.json.test.jsonb.model.CrateDeserializerWithConversion;
import net.gcolin.json.test.jsonb.model.CrateInner;
import net.gcolin.json.test.jsonb.model.CrateJsonObjectDeserializer;
import net.gcolin.json.test.jsonb.model.CrateSerializer;
import net.gcolin.json.test.jsonb.model.CrateSerializerWithConversion;

/**
 * Serializer tests.
 * 
 * @author Roman Grigoriadi
 */
public class SerializersTest {

  @Test
  public void testClassLevelAnnotation() {
    Crate crate = new Crate();
    crate.crateBigDec = BigDecimal.TEN;
    crate.crateStr = "crateStr";

    crate.annotatedType = new AnnotatedWithSerializerType();
    crate.annotatedType.value = "abc";
    crate.annotatedTypeOverridenOnProperty = new AnnotatedWithSerializerType();
    crate.annotatedTypeOverridenOnProperty.value = "def";
    final Jsonb jsonb = JsonbBuilder.create();
    String expected = "{\"crate_str\":\"crateStr\",\"crateBigDec\":10,\"annotatedType\":"
        + "{\"valueField\":\"replaced value\"},\"annotatedTypeOverridenOnProperty\":"
        + "{\"valueField\":\"overridden value\"}}";

    assertEquals(expected, jsonb.toJson(crate));

    Crate result = jsonb.fromJson(expected, Crate.class);
    assertEquals("replaced value", result.annotatedType.value);
    assertEquals("overridden value", result.annotatedTypeOverridenOnProperty.value);

  }

  /**
   * Tests JSONB deserialization of arbitrary type invoked from a Deserializer.
   */
  @Test
  public void testDeserialzierDeserializationByType() {
    JsonbConfig config = new JsonbConfig().withDeserializers(new CrateDeserializer());
    Jsonb jsonb = JsonbBuilder.create(config);

    Box box = createPojoWithDates();

    String expected =
        "{\"boxStr\":\"Box string\",\"crate\":{\"crateInner\":{\"crateInnerBigDec\":10,"
            + "\"crate_inner_str\":\"Single inner\",\"date\":\"14.05.2015 || 11:10:01\"},"
            + "\"crateInnerList\":[{\"crateInnerBigDec\":10,\"crate_inner_str\":"
            + "\"List inner 0\"},{\"crateInnerBigDec\":10,\"crate_inner_str\":\"List inner 1\"}],\"date\":\"2015-05-14T11:10:01\"},\"secondBoxStr\":\"Second box string\"}";

    Box result = jsonb.fromJson(expected, Box.class);

    // deserialized by deserializationContext.deserialize(Class c)
    assertEquals(box.crate.crateInner.crateInnerBigDec, result.crate.crateInner.crateInnerBigDec);
    assertEquals(box.crate.crateInner.crateInnerStr, result.crate.crateInner.crateInnerStr);

    assertEquals("List inner 0", result.crate.crateInnerList.get(0).crateInnerStr);
    assertEquals("List inner 1", result.crate.crateInnerList.get(1).crateInnerStr);

    // set by deserializer statically
    assertEquals(new BigDecimal("123"), result.crate.crateBigDec);
    assertEquals("abc", result.crate.crateStr);

  }

  /**
   * Tests JSONB serialization of arbitrary type invoked from a Serializer.
   */
  @Test
  public void testSerializerSerializationOfType() {
    JsonbConfig config = new JsonbConfig().withSerializers(new CrateSerializer());
    Jsonb jsonb = JsonbBuilder.create(config);
    String expected = "{\"boxStr\":\"Box string\",\"crate\":{\"crate_str\":\"REPLACED crate str\","
        + "\"crateInner\":{\"crate_inner_str\":\"Single inner\",\"crateInnerBigDec\":10},"
        + "\"crateInnerList\":[{\"crate_inner_str\":\"List inner 0\",\"crateInnerBigDec\":10},"
        + "{\"crate_inner_str\":\"List inner 1\",\"crateInnerBigDec\":10}],\"crateBigDec\":54321},"
        + "\"secondBoxStr\":\"Second box string\"}";
    Box pojo = createPojo();

    assertEquals(expected, jsonb.toJson(pojo));

    Box result = jsonb.fromJson(expected, Box.class);
    assertEquals(new BigDecimal("54321"), result.crate.crateBigDec);
    assertEquals("REPLACED crate str", result.crate.crateStr);
    assertEquals(pojo.crate.crateInner.crateInnerStr, result.crate.crateInner.crateInnerStr);
    assertEquals(pojo.crate.crateInner.crateInnerBigDec, result.crate.crateInner.crateInnerBigDec);
  }

  /**
   * Tests jsonb type conversion, including property customization.
   */
  @Test
  public void testDeserializersUsingConversion() {
    JsonbConfig config = new JsonbConfig().withDeserializers(new CrateDeserializerWithConversion());
    Jsonb jsonb = JsonbBuilder.create(config);

    String json =
        "{\"boxStr\":\"Box string\",\"crate\":{\"date-converted\":\"2015-05-14T11:10:01\","
            + "\"crateStr\":\"REPLACED crate str\",\"crateInner\":{\"crateInnerBigDec\":10,"
            + "\"crate_inner_str\":\"Single inner\",\"date\":\"14.05.2015 || 11:10:01\"},"
            + "\"crateBigDec\":54321},\"secondBoxStr\":\"Second box string\"}";
    Box result = jsonb.fromJson(json, Box.class);
    final Date expected = getExpectedDate();
    assertEquals(expected, result.crate.date);
    assertEquals("Box string", result.boxStr);
    assertEquals("Second box string", result.secondBoxStr);
  }

  private Date getExpectedDate() {
    return new Calendar.Builder().setTimeZone(TimeZone.getTimeZone("UTC")).setDate(2015, 4, 14)
        .setTimeOfDay(11, 10, 1).build().getTime();
  }

  @Test
  public void testSerializationUsingConversion() {
    JsonbConfig config = new JsonbConfig().withSerializers(new CrateSerializerWithConversion());
    Jsonb jsonb = JsonbBuilder.create(config);

    String json =
        "{\"boxStr\":\"Box string\",\"crate\":{\"crate_str\":\"REPLACED crate str\",\"crateInner\":"
            + "{\"crate_inner_str\":\"Single inner\",\"crateInnerBigDec\":10,\"date\":"
            + "\"14.05.2015 || 11:10:01\"},\"crateInnerList\":[{\"crate_inner_str\":\"List inner 0\","
            + "\"crateInnerBigDec\":10},{\"crate_inner_str\":\"List inner 1\","
            + "\"crateInnerBigDec\":10}],\"crateBigDec\":54321},\"secondBoxStr\":\"Second box string\"}";
    assertEquals(json, jsonb.toJson(createPojoWithDates()));
  }

  @Test
  public void testAnnotations() {
    final Jsonb jsonb = JsonbBuilder.create();
    BoxWithAnnotations box = new BoxWithAnnotations();
    box.boxStr = "Box string";
    box.secondBoxStr = "Second box string";
    box.crate = new Crate();
    box.crate.date = getExpectedDate();
    box.crate.crateInner = createCrateInner("Single inner");

    box.crate.crateInnerList = new ArrayList<>();
    box.crate.crateInnerList.add(createCrateInner("List inner 0"));
    box.crate.crateInnerList.add(createCrateInner("List inner 1"));

    // TODO fix / uncomment after keyname argument will be added to JsonbSerializer
    String expected =
        "{\"boxStr\":\"Box string\",\"crate\":{\"crate_str\":\"REPLACED crate str\",\"crateInner\":"
            + "{\"crate_inner_str\":\"Single inner\",\"crateInnerBigDec\":10},\"crateInnerList\":"
            + "[{\"crate_inner_str\":\"List inner 0\",\"crateInnerBigDec\":10},{\"crate_inner_str\":"
            + "\"List inner 1\",\"crateInnerBigDec\":10}],\"crateBigDec\":54321},"
            + "\"secondBoxStr\":\"Second box string\"}";

    assertEquals(expected, jsonb.toJson(box));

    BoxWithAnnotations result = jsonb.fromJson(expected, BoxWithAnnotations.class);

    // deserialized by deserializationContext.deserialize(Class c)
    assertEquals(box.crate.crateInner.crateInnerBigDec, result.crate.crateInner.crateInnerBigDec);
    assertEquals(box.crate.crateInner.crateInnerStr, result.crate.crateInner.crateInnerStr);

    assertEquals(2L, result.crate.crateInnerList.size());
    assertEquals("List inner 0", result.crate.crateInnerList.get(0).crateInnerStr);
    assertEquals("List inner 1", result.crate.crateInnerList.get(1).crateInnerStr);

    // set by deserializer statically
    assertEquals(new BigDecimal("123"), result.crate.crateBigDec);
    assertEquals("abc", result.crate.crateStr);
  }

  @Test
  public void testAnnotationsOverride() {
    JsonbConfig config = new JsonbConfig().withDeserializers(new CrateJsonObjectDeserializer())
        .withSerializers(new CrateSerializer());
    Jsonb jsonb = JsonbBuilder.create(config);

    BoxWithAnnotations box = new BoxWithAnnotations();
    box.boxStr = "Box string";
    box.secondBoxStr = "Second box string";
    box.crate = new Crate();
    box.crate.crateInner = createCrateInner("Single inner");
    box.crate.date = getExpectedDate();

    box.crate.crateInnerList = new ArrayList<>();
    box.crate.crateInnerList.add(createCrateInner("List inner 0"));
    box.crate.crateInnerList.add(createCrateInner("List inner 1"));

    // TODO fix / uncomment after keyname argument will be added to JsonbSerializer
    // String expected = "{\"boxStr\":\"Box string\",\"crate\":{\"crateStr\":\"REPLACED crate
    // str\",\"crateInner\":{\"crateInnerBigDec\":10,\"crate_inner_str\":\"Single
    // inner\"},\"crateInnerList\":[{\"crateInnerBigDec\":10,\"crate_inner_str\":\"List inner
    // 0\"},{\"crateInnerBigDec\":10,\"crate_inner_str\":\"List inner
    // 1\"}],\"crateBigDec\":54321,\"date-converted\":\"2015-05-14T11:10:01\"},\"secondBoxStr\":\"Second
    // box string\"}";
    String expected =
        "{\"boxStr\":\"Box string\",\"crate\":{\"crate_str\":\"REPLACED crate str\",\"crateInner\":"
            + "{\"crate_inner_str\":\"Single inner\",\"crateInnerBigDec\":10},\"crateInnerList\":"
            + "[{\"crate_inner_str\":\"List inner 0\",\"crateInnerBigDec\":10},{\"crate_inner_str\":"
            + "\"List inner 1\",\"crateInnerBigDec\":10}],\"crateBigDec\":54321},\"secondBoxStr\":\"Second box string\"}";

    assertEquals(expected, jsonb.toJson(box));

    BoxWithAnnotations result = jsonb.fromJson(expected, BoxWithAnnotations.class);

    // deserialized by deserializationContext.deserialize(Class c)
    assertEquals(box.crate.crateInner.crateInnerBigDec, result.crate.crateInner.crateInnerBigDec);
    assertEquals(box.crate.crateInner.crateInnerStr, result.crate.crateInner.crateInnerStr);

    assertEquals(2L, result.crate.crateInnerList.size());
    assertEquals("List inner 0", result.crate.crateInnerList.get(0).crateInnerStr);
    assertEquals("List inner 1", result.crate.crateInnerList.get(1).crateInnerStr);

    // set by deserializer statically
    assertEquals(new BigDecimal("123"), result.crate.crateBigDec);
    assertEquals("abc", result.crate.crateStr);
  }

  private Box createPojoWithDates() {
    Date date = getExpectedDate();
    Box box = createPojo();
    box.crate.date = date;
    box.crate.crateInner.date = date;
    return box;
  }

  private Box createPojo() {
    Box box = new Box();
    box.boxStr = "Box string";
    box.crate = new Crate();
    box.secondBoxStr = "Second box string";


    box.crate.crateInner = createCrateInner("Single inner");

    box.crate.crateInnerList = new ArrayList<>();
    box.crate.crateInnerList.add(createCrateInner("List inner 0"));
    box.crate.crateInnerList.add(createCrateInner("List inner 1"));

    return box;
  }

  private CrateInner createCrateInner(String name) {
    final CrateInner crateInner = new CrateInner();
    crateInner.crateInnerStr = name;
    crateInner.crateInnerBigDec = BigDecimal.TEN;
    return crateInner;
  }


}
