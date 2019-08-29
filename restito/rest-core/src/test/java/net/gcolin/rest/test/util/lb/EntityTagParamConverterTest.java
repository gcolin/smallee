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

import net.gcolin.rest.util.lb.EntityTagParamConverter;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.EntityTag;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class EntityTagParamConverterTest {

  @Test
  public void fromStringTest() {
    Assert.assertNull(new EntityTagParamConverter().fromString(null));
    Assert.assertNull(new EntityTagParamConverter().fromString(""));
    EntityTag tag = new EntityTagParamConverter().fromString("w/123");
    Assert.assertEquals("123", tag.getValue());
    Assert.assertTrue(tag.isWeak());

    tag = new EntityTagParamConverter().fromString("W/AS");
    Assert.assertEquals("AS", tag.getValue());
    Assert.assertTrue(tag.isWeak());

    tag = new EntityTagParamConverter().fromString("QWERTY");
    Assert.assertEquals("QWERTY", tag.getValue());
    Assert.assertFalse(tag.isWeak());
  }

  @Test
  public void toStringTest() {
    Assert.assertEquals("W/AD", new EntityTagParamConverter().toString(new EntityTag("AD", true)));
    Assert.assertEquals("AD", new EntityTagParamConverter().toString(new EntityTag("AD")));
    Assert.assertEquals("AD", new EntityTagParamConverter().toString(new EntityTag("AD", false)));
  }

}
