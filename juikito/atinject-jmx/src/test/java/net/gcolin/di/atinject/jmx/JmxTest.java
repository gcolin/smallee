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
package net.gcolin.di.atinject.jmx;

import java.lang.management.ManagementFactory;

import javax.inject.Singleton;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JmxTest {

  @Singleton
  public static class A {

    @Jmx
    int val;

  }

  @Singleton
  public static class B {

    @Jmx
    int square(int val) {
      return val * val;
    }

  }

  @Singleton
  public static class C {

    boolean useGetter;
    boolean useSetter;

    @Jmx
    int val;

    @Jmx
    public int getVal() {
      useGetter = true;
      return val;
    }

    @Jmx
    public void setVal(int val) {
      useSetter = true;
      this.val = val;
    }

  }

  @Singleton
  public static class D {

    @Jmx("hello")
    String val;

  }

  @Singleton
  public static class E {

    @Jmx(description = "a value")
    String val;

  }

  public static class F {

    @JmxAttribute
    String name;

    @Jmx
    String val;

  }

  @Test
  public void testField()
      throws InstanceNotFoundException, InvalidAttributeValueException, AttributeNotFoundException,
      MalformedObjectNameException, ReflectionException, MBeanException {
    Environment env = new Environment();
    env.add(A.class);
    env.start();
    try {

      A a = env.get(A.class);
      Assert.assertEquals(0, a.val);

      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      server.setAttribute(new ObjectName("net.gcolin.di.atinject.jmx.JmxTest:type=A"),
          new Attribute("val", 7));

      Assert.assertEquals(7, a.val);
    } finally {
      env.stop();
    }
  }

  @Test
  public void testOperation()
      throws InstanceNotFoundException, InvalidAttributeValueException, AttributeNotFoundException,
      MalformedObjectNameException, ReflectionException, MBeanException {
    Environment env = new Environment();
    env.add(B.class);
    env.start();
    try {

      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      Integer r =
          (Integer) server.invoke(new ObjectName("net.gcolin.di.atinject.jmx.JmxTest:type=B"),
              "square", new Object[] {7}, new String[] {int.class.getName()});

      Assert.assertEquals(49, r.intValue());
    } finally {
      env.stop();
    }
  }

  @Test
  public void testGetterSetter()
      throws InstanceNotFoundException, InvalidAttributeValueException, AttributeNotFoundException,
      MalformedObjectNameException, ReflectionException, MBeanException {
    Environment env = new Environment();
    env.add(C.class);
    env.start();
    try {

      C c = env.get(C.class);
      Assert.assertFalse(c.useGetter);
      Assert.assertFalse(c.useSetter);
      Assert.assertEquals(0, c.val);

      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      server.setAttribute(new ObjectName("net.gcolin.di.atinject.jmx.JmxTest:type=C"),
          new Attribute("val", 7));

      Assert.assertFalse(c.useGetter);
      Assert.assertTrue(c.useSetter);
      Assert.assertEquals(7, c.val);

      Assert.assertEquals(7,
          server.getAttribute(new ObjectName("net.gcolin.di.atinject.jmx.JmxTest:type=C"), "val"));

      Assert.assertTrue(c.useGetter);
      Assert.assertTrue(c.useSetter);
      Assert.assertEquals(7, c.val);
    } finally {
      env.stop();
    }
  }

  @Test
  public void testFieldName()
      throws InstanceNotFoundException, InvalidAttributeValueException, AttributeNotFoundException,
      MalformedObjectNameException, ReflectionException, MBeanException {
    Environment env = new Environment();
    env.add(D.class);
    env.start();
    try {
      D a = env.get(D.class);
      Assert.assertNull(a.val);

      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      server.setAttribute(new ObjectName("net.gcolin.di.atinject.jmx.JmxTest:type=D"),
          new Attribute("hello", "world"));

      Assert.assertEquals("world", a.val);
    } finally {
      env.stop();
    }
  }

  @Test
  public void testDescription()
      throws InstanceNotFoundException, InvalidAttributeValueException, AttributeNotFoundException,
      MalformedObjectNameException, ReflectionException, MBeanException, IntrospectionException {
    Environment env = new Environment();
    env.add(E.class);
    env.start();
    try {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      MBeanAttributeInfo beanInfo =
          server.getMBeanInfo(new ObjectName("net.gcolin.di.atinject.jmx.JmxTest:type=E"))
              .getAttributes()[0];

      Assert.assertEquals("a value", beanInfo.getDescription());
    } finally {
      env.stop();
    }
  }

  @Test
  public void testSingleton() throws Exception {
    Environment env = new Environment();
    env.start();
    JmxExtension ext = env.getExtension(JmxExtension.class);
    for (int i = 0; i < 3; i++) {
      F f = new F();
      f.name = "f" + i;
      f.val = "hello" + i;
      ext.add(f);
    }

    try {
      MBeanServer server = ManagementFactory.getPlatformMBeanServer();
      for (int i = 0; i < 3; i++) {
        Assert.assertEquals("hello" + i, server.getAttribute(
            new ObjectName("net.gcolin.di.atinject.jmx.JmxTest:type=F,name=f" + i), "val"));
      }
    } finally {
      env.stop();
    }
  }

}
