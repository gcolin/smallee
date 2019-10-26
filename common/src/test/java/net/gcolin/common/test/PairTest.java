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

import net.gcolin.common.lang.Pair;

import org.junit.Assert;
import org.junit.Test;

public class PairTest {

  @SuppressWarnings("unlikely-arg-type")
@Test
  public void test() {
    Pair<String, String> pair = new Pair<>();
    Assert.assertNull(pair.getKey());
    Assert.assertNull(pair.getLeft());
    Assert.assertNull(pair.getRight());
    Assert.assertNull(pair.getValue());

    pair = new Pair<>("hello", "world");
    
    Pair<String, String> pair2 = Pair.of("hello", "world");
    Assert.assertEquals(pair, pair2);
    Assert.assertEquals("Pair [key=hello, value=world]", pair2.toString());
    Assert.assertFalse(pair2.equals(null));
    Assert.assertTrue(pair2.equals(pair));
    Assert.assertFalse(pair2.equals("false"));
    Assert.assertFalse(pair.equals(Pair.of("hello", "world2")));
    Assert.assertFalse(pair.equals(Pair.of("hello2", "world")));
    
    Assert.assertEquals("hello", pair.getKey());
    Assert.assertEquals("hello", pair.getLeft());
    Assert.assertEquals("world", pair.getRight());
    Assert.assertEquals("world", pair.getValue());

    Assert.assertEquals("world", pair.setValue("newValue"));
    Assert.assertEquals("newValue", pair.getValue());

    pair.setRight("h");
    Assert.assertEquals("h", pair.getValue());
    Assert.assertEquals("h", pair.getRight());

    pair.setKey("k");
    Assert.assertEquals("k", pair.getKey());

    pair.setLeft("k");
    Assert.assertEquals("k", pair.getKey());
    Assert.assertEquals("k", pair.getLeft());
  }
}
