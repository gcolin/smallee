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
import net.gcolin.common.reflect.Reflect;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BeanTest {

  public static class IPair extends Pair<Integer, String> {

  }

  public static class Obj {
    public List<String> list;
  }

  public abstract static class AObj<T> {
    public List<T> list;
  }

  public static class IObj extends AObj<String> {
  }

  @Test
  public void test() {
    List<Class<?>> list = Reflect.getTypeArguments(Pair.class, IPair.class, null);
    Assert.assertNotNull(list);
    Assert.assertEquals(2, list.size());
    Assert.assertEquals(Integer.class, list.get(0));
    Assert.assertEquals(String.class, list.get(1));
  }

  @SuppressWarnings("serial")
  @Test
  public void testAnonymous() {
    List<Type> list = Reflect.getGenericTypeArguments(Collection.class,
        new ArrayList<String>() {}.getClass(), null);
    Assert.assertEquals(String.class, list.get(0));
  }

  @Test
  public void testListField() throws NoSuchFieldException, SecurityException {
    List<Type> list = Reflect.getGenericTypeArguments(Collection.class,
        Obj.class.getField("list").getGenericType(), Obj.class);
    Assert.assertEquals(String.class, list.get(0));
  }

  @Test
  public void testListFieldWithInheritance() throws NoSuchFieldException, SecurityException {
    List<Type> list = Reflect.getGenericTypeArguments(Collection.class,
        IObj.class.getField("list").getGenericType(), IObj.class);
    Assert.assertEquals(String.class, list.get(0));
  }

  @Test
  public void testNavigableSet() throws ClassNotFoundException {
    List<Type> list = Reflect.getGenericTypeArguments(Collection.class,
        Reflect.parseAsGeneric("java.util.NavigableSet<java.lang.String>"), null);
    Assert.assertEquals(String.class, list.get(0));
  }

}
