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

package net.gcolin.rest.test;

import net.gcolin.rest.RuntimeDelegateImpl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class VariantListBuilderImplTest {

  VariantListBuilder vbuilder;

  @Before
  public void before() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    vbuilder = RuntimeDelegate.getInstance().createVariantListBuilder();
  }

  @Test
  public void encodingTest() {
    List<Variant> list = vbuilder.encodings("gzip").add().build();
    Assert.assertEquals(1, list.size());
    assertEncoding(list, 0);
  }

  private void assertEncoding(List<Variant> list, int index) {
    Assert.assertEquals("gzip", list.get(index).getEncoding());
    Assert.assertNull(list.get(index).getLanguage());
    Assert.assertNull(list.get(index).getMediaType());
  }

  @Test
  public void languageTest() {
    List<Variant> list = vbuilder.languages(Locale.FRENCH).add().build();
    Assert.assertEquals(1, list.size());
    assertLanguage(list, 0);
  }

  private void assertLanguage(List<Variant> list, int index) {
    Assert.assertEquals(Locale.FRENCH, list.get(index).getLanguage());
    Assert.assertNull(list.get(index).getEncoding());
    Assert.assertNull(list.get(index).getMediaType());
  }

  @Test
  public void mediaTypeTest() {
    List<Variant> list = vbuilder.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add().build();
    Assert.assertEquals(1, list.size());
    assertMediaType(list, 0);
  }

  private void assertMediaType(List<Variant> list, int index) {
    Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, list.get(index).getMediaType());
    Assert.assertNull(list.get(index).getEncoding());
    Assert.assertNull(list.get(index).getLanguage());
  }

  @Test
  public void emptyTest() {
    Assert.assertTrue(vbuilder.add().build().isEmpty());
  }

  @Test
  public void chaningTest() {
    List<Variant> list = vbuilder.mediaTypes(MediaType.APPLICATION_JSON_TYPE).add()
        .languages(Locale.FRENCH).add().encodings("gzip").add().build();
    Assert.assertEquals(3, list.size());
    assertMediaType(list, 0);
    assertLanguage(list, 1);
    assertEncoding(list, 2);
  }

  @Test
  public void multipleTest() {
    List<Variant> list =
        vbuilder.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.APPLICATION_ATOM_XML_TYPE)
            .languages(Locale.FRANCE).encodings("deflate").add().build();
    Assert.assertEquals(2, list.size());
    Assert.assertEquals(MediaType.APPLICATION_JSON_TYPE, list.get(0).getMediaType());
    Assert.assertEquals("deflate", list.get(0).getEncoding());
    Assert.assertEquals(Locale.FRANCE, list.get(0).getLanguage());
    Assert.assertEquals(MediaType.APPLICATION_ATOM_XML_TYPE, list.get(1).getMediaType());
    Assert.assertEquals("deflate", list.get(1).getEncoding());
    Assert.assertEquals(Locale.FRANCE, list.get(1).getLanguage());
  }

}
