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

package net.gcolin.rest.ext.xml.test;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.ext.xml.SourceProvider;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.xml.transform.Source;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class SourceProviderTest<T extends Source, P extends SourceProvider<T>> {

  String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><hello>world</hello>";

  abstract P getProvider();

  @Test
  public void getSizeTest() {
    Assert.assertEquals(-1, getProvider().getSize(null, Boolean.class, Boolean.class, null,
        MediaType.APPLICATION_XML_TYPE));
  }

  protected void writeToTest0(T source) throws IOException {
    Class<?> type = Reflect.getTypeArguments(SourceProviderTest.class, getClass(), null).get(0);
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    MultivaluedHashMap<String, Object> headers = new MultivaluedHashMap<>();
    getProvider().writeTo(source, type, type, new Annotation[0], MediaType.APPLICATION_XML_TYPE,
        headers, bout);
    Assert.assertEquals(xml, new String(bout.toByteArray(), StandardCharsets.UTF_8));
  }

}
