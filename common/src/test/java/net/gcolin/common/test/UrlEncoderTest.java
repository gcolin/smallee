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

import net.gcolin.common.lang.UrlEncoder;

import org.junit.Assert;
import org.junit.Test;

public class UrlEncoderTest {

  @Test
  public void removeTest() {
    UrlEncoder encoder = new UrlEncoder();
    Assert.assertEquals("%2F", encoder.encode("/"));
    encoder.remove('/');
    Assert.assertEquals("/", encoder.encode("/"));
    encoder.add('/');
    Assert.assertEquals("%2F", encoder.encode("/"));
  }

  @Test
  public void addTest() {
    UrlEncoder encoder = new UrlEncoder();
    Assert.assertEquals("a", encoder.encode("a"));
    encoder.add('a');
    Assert.assertEquals("%61", encoder.encode("a"));
    encoder.remove('a');
    Assert.assertEquals("a", encoder.encode("a"));
  }

}
