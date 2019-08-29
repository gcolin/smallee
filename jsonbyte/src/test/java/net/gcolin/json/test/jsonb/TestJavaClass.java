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

/**
 * @author Gaël COLIN
 */
public class TestJavaClass extends AbstractMultiCharsetTest {

  public static class Obj {

    private String field;
    final int finalField = 1;
    private int intfield;
    private Nested nested;
    private StaticNested staticnested;

    public Nested getNested() {
      return nested;
    }

    int getIntfield() {
      return intfield;
    }

    void setIntfield(int intfield) {
      this.intfield = intfield;
    }

    public int getNumber() {
      return intfield;
    }

    public void setNumber(int intfield) {
      this.intfield = intfield;
    }

    class Nested {
      String value;
    }

    static class StaticNested {
      String value;
    }

  }

  @Test
  public void fieldAndScopeTest() {
    Obj obj = new Obj();
    obj.intfield = 3;
    obj.field = "hello";

    Obj o2 = test0(Obj.class, obj, "{\"field\":\"hello\",\"number\":3}");
    Assert.assertEquals(3, o2.intfield);
    Assert.assertEquals("hello", o2.field);
  }

  @Test
  public void nestedClassTest() {
    Obj obj = new Obj();
    obj.nested = obj.new Nested();
    obj.nested.value = "hello";

    Obj o2 = test0(Obj.class, obj, "{\"nested\":{\"value\":\"hello\"},\"number\":0}");
    Assert.assertEquals("hello", o2.nested.value);
  }

  @Test
  public void staticNestedClassTest() {
    Obj obj = new Obj();
    obj.staticnested = new Obj.StaticNested();
    obj.staticnested.value = "hello";

    Obj o2 = test0(Obj.class, obj, "{\"staticnested\":{\"value\":\"hello\"},\"number\":0}");
    Assert.assertEquals("hello", o2.staticnested.value);
  }

}
