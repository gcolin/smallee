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

package net.gcolin.rest.test.provider;

import net.gcolin.rest.provider.FormProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class FormProviderTest {

  String formText = "a=hello&a=world&b=!";

  @Test
  public void writeToTest() throws IOException {
    MultivaluedMap<String, String> form = new MultivaluedHashMap<>();
    form.add("a", "hello");
    form.add("a", "world");
    form.add("b", "!");

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    new FormProvider().writeTo(form, MultivaluedMap.class, MultivaluedMap.class, new Annotation[0],
        MediaType.APPLICATION_FORM_URLENCODED_TYPE, null, bout);

    Assert.assertEquals(formText, new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void readFromTest() throws IOException {
    MultivaluedMap<String, String> form = new FormProvider().readFrom((Class) MultivaluedMap.class,
        MultivaluedMap.class, new Annotation[0], MediaType.APPLICATION_FORM_URLENCODED_TYPE, null,
        new ByteArrayInputStream(formText.getBytes(StandardCharsets.UTF_8)));
    Assert.assertEquals(Arrays.asList("hello", "world"), form.get("a"));
    Assert.assertEquals(Arrays.asList("!"), form.get("b"));
  }

}
