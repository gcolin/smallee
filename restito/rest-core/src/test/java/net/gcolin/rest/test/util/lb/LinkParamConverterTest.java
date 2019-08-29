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

package net.gcolin.rest.test.util.lb;

import net.gcolin.rest.util.lb.LinkParamConverter;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Link;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class LinkParamConverterTest {

  @Test
  public void fromStringTest() {
    Link link =
        new LinkParamConverter().fromString("<http://www.acme.com/corporate.css>; REL=stylesheet");
    Assert.assertEquals("http://www.acme.com/corporate.css", link.getUri().toString());
    Assert.assertEquals(1, link.getParams().size());
    Assert.assertEquals("stylesheet", link.getRel());

    link = new LinkParamConverter()
        .fromString(" <compact.css>; rel=\"stylesheet\"; title=\"compact\"");
    Assert.assertEquals("compact.css", link.getUri().toString());
    Assert.assertEquals(2, link.getParams().size());
    Assert.assertEquals("stylesheet", link.getRel());
    Assert.assertEquals("stylesheet", link.getRels().get(0));
    Assert.assertEquals("compact", link.getTitle());

    link =
        new LinkParamConverter().fromString(" <compact.css>; title=\"compact\"; type=\"text/css\"");
    Assert.assertEquals("compact.css", link.getUri().toString());
    Assert.assertEquals(2, link.getParams().size());
    Assert.assertNull(link.getRel());
    Assert.assertTrue(link.getRels().isEmpty());
    Assert.assertEquals("compact", link.getTitle());
    Assert.assertEquals("text/css", link.getType());

    try {
      new LinkParamConverter().fromString("compact.css");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }

    try {
      new LinkParamConverter().fromString("<compact.css");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void toStringTest() {
    String[] values = {"<http://www.acme.com/corporate.css> ; rel=\"stylesheet\"",
        "<compact.css> ; rel=\"stylesheet\"; title=\"compact\"",
        "<compact.css> ; title=\"compact\"; type=\"text/css\""};
    for (String value : values) {
      Assert.assertEquals(value,
          new LinkParamConverter().toString(new LinkParamConverter().fromString(value)));
    }
  }

}
