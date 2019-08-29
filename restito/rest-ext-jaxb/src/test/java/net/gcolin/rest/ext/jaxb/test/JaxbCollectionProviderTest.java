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

package net.gcolin.rest.ext.jaxb.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.gcolin.common.reflect.BeanAccess;
import net.gcolin.rest.ext.jaxb.JaxbCollectionProvider;
import net.gcolin.rest.ext.jaxb.JaxbContextResolver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class JaxbCollectionProviderTest {

  @XmlRootElement
  public static class Element {

    String value;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

  }

  @XmlRootElement(
      name = "other")
  public static class Element2 {

    String value;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

  }

  String elements = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
      + "<elements><element><value>1</value></element>"
      + "<element><value>2</value></element></elements>";
  List<Element> list;
  Element e1;
  Element e2;
  Element2 e21;
  Element2 e22;
  JaxbCollectionProvider provider;

  /**
   * Initialize the test.
   */
  @Before
  public void before() {
    provider = new JaxbCollectionProvider();
    BeanAccess.setProperty(provider, "contextResolver", new JaxbContextResolver());

    e1 = new Element();
    e2 = new Element();
    e21 = new Element2();
    e22 = new Element2();

    e1.setValue("1");
    e2.setValue("2");
    e21.setValue("3");
    e22.setValue("4");
  }

  @Test
  public void isWriteableTest() {
    assertFalse(
        provider.isWriteable(String.class, String.class, null, MediaType.APPLICATION_XML_TYPE));
    assertTrue(
        provider.isWriteable(String[].class, String[].class, null, MediaType.APPLICATION_XML_TYPE));
    assertFalse(provider.isWriteable(String[].class, String[].class, null,
        MediaType.APPLICATION_JSON_TYPE));

    assertTrue(provider.isWriteable(List.class, List.class, null, MediaType.APPLICATION_XML_TYPE));
    assertFalse(
        provider.isWriteable(List.class, List.class, null, MediaType.APPLICATION_JSON_TYPE));
  }

  @Test
  public void getSizeTest() {
    assertEquals(-1, provider.getSize(null, null, null, null, null));
  }

  @Test
  public void writeToCollectionGenericTest() throws Exception {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();

    Field field = JaxbCollectionProviderTest.class.getDeclaredField("list");
    provider.writeTo(Arrays.asList(e1, e2), field.getType(), field.getGenericType(),
        new Annotation[0], MediaType.APPLICATION_XML_TYPE, headers, bout);
    Assert.assertEquals(elements, new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void writeToCollectionTest() throws Exception {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    provider.writeTo(Arrays.asList(e1, e2), List.class, List.class, new Annotation[0],
        MediaType.APPLICATION_XML_TYPE, headers, bout);
    Assert.assertEquals(elements, new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void writeToArrayTest() throws Exception {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    provider.writeTo(Arrays.asList(e1, e2).toArray(new Element[2]), Element[].class,
        Element[].class, new Annotation[0], MediaType.APPLICATION_XML_TYPE, headers, bout);
    Assert.assertEquals(elements, new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @Test
  public void writeToUndefinedTest() throws Exception {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    provider.writeTo(new ArrayList<>(), List.class, List.class, new Annotation[0],
        MediaType.APPLICATION_XML_TYPE, headers, bout);
    Assert.assertEquals(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><undefineds></undefineds>",
        new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void readFromTest() throws Exception {
    MultivaluedHashMap<String, String> headers = new MultivaluedHashMap<>();

    Field field = JaxbCollectionProviderTest.class.getDeclaredField("list");
    List<Element> list = (List<Element>) provider.readFrom((Class<Object>) field.getType(),
        field.getGenericType(), new Annotation[0], MediaType.APPLICATION_XML_TYPE, headers,
        new ByteArrayInputStream(elements.getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals(2, list.size());
    Assert.assertTrue(list.get(0) instanceof Element);
    Assert.assertTrue(list.get(1) instanceof Element);
    Assert.assertEquals("1", list.get(0).getValue());
    Assert.assertEquals("2", list.get(1).getValue());
  }

}
