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

import net.gcolin.rest.util.lb.IntParamConverter;
import net.gcolin.rest.util.lb.NotNullableConverter;

import org.junit.Assert;
import org.junit.Test;

/** 
 * @author Gaël COLIN
 * @since 1.0
 */
public class NotNullableConverterTest {

  private NotNullableConverter<Integer> converter =
      new NotNullableConverter<>(new IntParamConverter(), 0);

  @Test
  public void fromStringTest() {
    Assert.assertEquals(0, converter.fromString(null).intValue());
    Assert.assertEquals(0, converter.fromString("").intValue());
    Assert.assertEquals(0, converter.fromString("abc").intValue());
    Assert.assertEquals(2, converter.fromString("2").intValue());
  }

  @Test
  public void toStringTest() {
    Assert.assertEquals("10", converter.toString(10));
  }

}
