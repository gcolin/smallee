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

package net.gcolin.json.test.jsonb;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 * Cache tests.
 * 
 * @author Gaël COLIN
 */
public class TestCache {

  public static class ElementNumber {
    int number;
  }

  public static class ElementString {
    String string;
  }

  public static class AbstractList<T> {
    List<T> list;
  }

  public static class NumberList extends AbstractList<ElementNumber> {

  }

  public static class StringList extends AbstractList<ElementString> {

  }

  @Test
  public void typeValueTest() {
    Jsonb jsonb = JsonbBuilder.create();
    NumberList o1 = new NumberList();
    ElementNumber en1 = new ElementNumber();
    en1.number = 1;
    ElementNumber en2 = new ElementNumber();
    en2.number = 2;
    o1.list = Arrays.asList(en1, en2);
    Assert.assertEquals("{\"list\":[{\"number\":1},{\"number\":2}]}", jsonb.toJson(o1));
    ElementString es1 = new ElementString();
    es1.string = "hello";
    ElementString es2 = new ElementString();
    es1.string = "world";
    StringList o2 = new StringList();
    o2.list = Arrays.asList(es1, es2);
    jsonb.toJson(o2);
  }
}
