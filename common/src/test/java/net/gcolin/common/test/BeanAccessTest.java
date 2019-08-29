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
import net.gcolin.common.reflect.BeanAccess;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BeanAccessTest {

  public static class A {
    String value;
  }

  public static class B {
    A otherName;
    String value;

    public A getA() {
      return otherName;
    }
  }

  public static class C {
    B propb;
    String value;
    String obj;

    public void setObj(String obj) {
      this.obj = obj;
    }
  }

  private C cobj;

  /**
   * Initialize cobj.
   */
  @Before
  public void before() {
    cobj = new C();
    cobj.propb = new B();
    cobj.propb.otherName = new A();
  }

  @Test
  public void setPropertyTest() {
    try {
      BeanAccess.setProperty(null, "hello", "world");
      Assert.fail("bean cannot be null");
    } catch (IllegalArgumentException ex) {
      // ok
    }

    try {
      BeanAccess.setProperty(this, null, "world");
      Assert.fail("property cannot be null");
    } catch (IllegalArgumentException ex) {
      // ok
    }

    Assert.assertNull(cobj.obj);
    BeanAccess.setProperty(cobj, "obj", "hello");
    Assert.assertEquals("hello", cobj.obj);

    Assert.assertNull(cobj.value);
    BeanAccess.setProperty(cobj, "value", "hello");
    Assert.assertEquals("hello", cobj.value);

    Assert.assertNull(cobj.propb.value);
    BeanAccess.setProperty(cobj, "propb.value", "hello");
    Assert.assertEquals("hello", cobj.propb.value);

    Assert.assertNull(cobj.propb.getA().value);
    BeanAccess.setProperty(cobj, "propb.a.value", "hello");
    Assert.assertEquals("hello", cobj.propb.getA().value);
  }

  @Test
  public void getPropertyTest() {
    try {
      BeanAccess.getProperty(null, "hello");
      Assert.fail("bean cannot be null");
    } catch (IllegalArgumentException ex) {
      // ok
    }

    try {
      BeanAccess.getProperty(this, null);
      Assert.fail("property cannot be null");
    } catch (IllegalArgumentException ex) {
      // ok
    }

    Assert.assertNull(BeanAccess.getProperty(cobj, "value"));
    cobj.value = "hello";
    Assert.assertEquals("hello", BeanAccess.getProperty(cobj, "value"));

    Assert.assertNull(BeanAccess.getProperty(cobj, "propb.value"));
    cobj.propb.value = "hello";
    Assert.assertEquals("hello", BeanAccess.getProperty(cobj, "propb.value"));

    Assert.assertNull(BeanAccess.getProperty(cobj, "propb.a.value"));
    cobj.propb.getA().value = "hello";
    Assert.assertEquals("hello", BeanAccess.getProperty(cobj, "propb.a.value"));
  }

  @Test
  public void getterTest() {
    try {
      BeanAccess.getter(null, "hello");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }

    try {
      BeanAccess.getter(Pair.class, null);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void getterFieldTest() {
    try {
      BeanAccess.getter(Pair.class, "z");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void setterTest() {
    try {
      BeanAccess.setter(null, "hello");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }

    try {
      BeanAccess.setter(Pair.class, null);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  @Test
  public void setterFieldTest() {
    try {
      BeanAccess.setterField(Pair.class, "z");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }
}
