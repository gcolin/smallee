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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.common.lang.CharIterator;
import net.gcolin.common.lang.Pair;
import net.gcolin.common.reflect.Reflect;

public class ReflectTest {

  Pair<Integer, String> pair;

  @Retention(RUNTIME)
  @Target({TYPE})
  public @interface XmlRootElement {
  }

  @Retention(RUNTIME)
  @Target({TYPE})
  public @interface XmlType {
  }

  @Retention(RUNTIME)
  @Target({FIELD, METHOD, TYPE})
  public @interface XmlTransient {
  }

  @XmlRootElement
  @XmlType
  public static class A {

    String string;

    public String getString(String val) {
      return null;
    }

    public String getString() {
      return string;
    }

    public boolean isTrue() {
      return true;
    }

    public void other() {}
  }

  public static class WillFail {
    public WillFail() {
      throw new IllegalStateException();
    }
  }

  public abstract static class StringCollection implements Collection<String> {

  }

  public abstract static class MegaCharIterator implements CharIterator {

  }

  @Test
  public void getClassTest() throws NoSuchFieldException, SecurityException {
    Assert.assertEquals(Pair.class, Reflect.toClass(Pair.class));
    Type genericType = ReflectTest.class.getDeclaredField("pair").getGenericType();
    Assert.assertFalse(genericType instanceof Class);
    Assert.assertTrue(genericType instanceof ParameterizedType);
    Assert.assertEquals(Pair.class, Reflect.toClass(genericType));

    Type type = Pair.class.getDeclaredField("key").getGenericType();
    Assert.assertEquals(Object.class, Reflect.toClass(type));
  }

  @Test
  public void testHasAnnotation() {
    Assert.assertTrue(Reflect.hasAnnotation(A.class.getAnnotations(), XmlType.class));
    Assert.assertFalse(Reflect.hasAnnotation(A.class.getAnnotations(), XmlTransient.class));
  }

  @Test
  public void testToNonPrimitiveEquivalent() {
    Assert.assertEquals(String.class, Reflect.toNonPrimitiveEquivalent(String.class));
    Assert.assertEquals(Integer.class, Reflect.toNonPrimitiveEquivalent(int.class));
    Assert.assertEquals(Long.class, Reflect.toNonPrimitiveEquivalent(long.class));
    Assert.assertEquals(Float.class, Reflect.toNonPrimitiveEquivalent(float.class));
    Assert.assertEquals(Double.class, Reflect.toNonPrimitiveEquivalent(double.class));
    Assert.assertEquals(Short.class, Reflect.toNonPrimitiveEquivalent(short.class));
    Assert.assertEquals(Boolean.class, Reflect.toNonPrimitiveEquivalent(boolean.class));
    Assert.assertEquals(Character.class, Reflect.toNonPrimitiveEquivalent(char.class));
    Assert.assertEquals(Byte.class, Reflect.toNonPrimitiveEquivalent(byte.class));
  }

  @Test
  public void testToPrimitiveEquivalent() {
    Assert.assertEquals(String.class, Reflect.toPrimitiveEquivalent(String.class));
    Assert.assertEquals(int.class, Reflect.toPrimitiveEquivalent(Integer.class));
    Assert.assertEquals(long.class, Reflect.toPrimitiveEquivalent(Long.class));
    Assert.assertEquals(float.class, Reflect.toPrimitiveEquivalent(Float.class));
    Assert.assertEquals(double.class, Reflect.toPrimitiveEquivalent(Double.class));
    Assert.assertEquals(short.class, Reflect.toPrimitiveEquivalent(Short.class));
    Assert.assertEquals(boolean.class, Reflect.toPrimitiveEquivalent(Boolean.class));
    Assert.assertEquals(char.class, Reflect.toPrimitiveEquivalent(Character.class));
    Assert.assertEquals(byte.class, Reflect.toPrimitiveEquivalent(Byte.class));
  }

  @Test
  public void testFirst() {
    Assert.assertNull(Reflect.first().apply(null, 0));

    List<List<String>> list = new ArrayList<>();
    list.add(new ArrayList<String>());
    list.get(0).add("hello");

    BiFunction<List<List<String>>, Integer, String> fun = Reflect.first();
    Assert.assertEquals("hello", fun.apply(list, 1));
  }

  @Test
  public void testArray() {
    List<List<String>> list = new ArrayList<>();
    list.add(new ArrayList<String>());
    list.get(0).add("hello");

    String[][] array = Reflect.array(String.class).apply(list, 0);
    Assert.assertEquals(1, array.length);
    Assert.assertEquals(1, array[0].length);
    Assert.assertEquals("hello", array[0][0]);
  }

  @Test
  public void testFlat() {
    List<List<String>> list = new ArrayList<>();
    list.add(new ArrayList<String>());
    list.get(0).add("hello");

    String[] array = Reflect.flat(String.class).apply(list, 1);
    Assert.assertEquals(1, array.length);
    Assert.assertEquals("hello", array[0]);
  }

  @Test
  public void testFlatList() {
    List<List<String>> list = new ArrayList<>();
    list.add(new ArrayList<String>());
    list.get(0).add("hello");

    BiFunction<List<List<String>>, Integer, List<String>> fun = Reflect.flatList();
    List<String> array = fun.apply(list, 0);
    Assert.assertEquals(1, array.size());
    Assert.assertEquals("hello", array.get(0));
  }

  @Test
  public void testNewInstance() {
    Assert.assertTrue(
        Reflect.newInstance(StringBuilder.class.getName()).getClass() == StringBuilder.class);
    Assert.assertNull(Reflect.newInstance(StringBuilder.class.getName() + "q"));

    Assert.assertTrue(Reflect.newInstance(StringBuilder.class).getClass() == StringBuilder.class);
    Assert.assertNull(Reflect.newInstance(WillFail.class));
  }

  @Test
  public void testIsGetter() throws NoSuchMethodException, SecurityException {
    Assert.assertTrue(Reflect.isGetter(A.class.getMethod("getString")));
    Assert.assertTrue(Reflect.isGetter(A.class.getMethod("isTrue")));
    Assert.assertFalse(Reflect.isGetter(A.class.getMethod("other")));
    Assert.assertFalse(Reflect.isGetter(A.class.getMethod("getString", String.class)));
  }

  @Test
  public void testGetPropertyName() throws NoSuchMethodException, SecurityException {
    Assert.assertEquals("string", Reflect.getPropertyName(A.class.getMethod("getString")));
    Assert.assertEquals("true", Reflect.getPropertyName(A.class.getMethod("isTrue")));
  }

  @Test
  public void testGetFieldByName() throws NoSuchMethodException, SecurityException {
    Assert.assertNotNull(Reflect.getFieldByName(A.class, "string"));
    Assert.assertNull(Reflect.getFieldByName(A.class, "hello"));
  }

  @Test
  public void testFindGetter() throws NoSuchMethodException, SecurityException {
    Assert.assertNotNull(Reflect.findGetter("string", A.class));
    Assert.assertNull(Reflect.findGetter("hello", A.class));
  }

  @Test
  public void testGetGetter() throws NoSuchMethodException, SecurityException {
    Assert.assertNotNull(Reflect.getGetter(A.class, "string", String.class));
    Assert.assertNull(Reflect.getGetter(A.class, "hello", String.class));
    Assert.assertNull(Reflect.getGetter(A.class, "string", Integer.class));
  }

  @Test
  public void hasAnnotationTest() throws NoSuchMethodException, SecurityException {
    Assert.assertFalse(
        Reflect.hasAnnotation(ReflectTest.class.getAnnotations(), Test.class.getName()));
    Assert.assertTrue(Reflect.hasAnnotation(
        ReflectTest.class.getMethod("hasAnnotationTest").getAnnotations(), Test.class.getName()));
  }

  @Test
  public void findTest() throws NoSuchMethodException, SecurityException {
    List<String> list =
        Reflect.find("hello,world", x -> x.split(","), x -> true, Function.identity());
    Assert.assertNotNull(list);
    Assert.assertEquals(2, list.size());
    Assert.assertTrue(list.contains("hello"));
    Assert.assertTrue(list.contains("world"));

    list =
        Reflect.find("hello,world", x -> x.split(","), x -> x.equals("world"), Function.identity());
    Assert.assertNotNull(list);
    Assert.assertEquals(1, list.size());
    Assert.assertTrue(list.contains("world"));

    Assert.assertNull(Reflect.find(ReflectTest.class, Reflect.FIELD_ITERATOR,
        x -> x.isAnnotationPresent(Test.class), Reflect.array(Field.class)));
    Assert.assertNull(Reflect.find(Object.class, Reflect.METHOD_ITERATOR, x -> true,
        Reflect.array(Method.class)));
    Method[][] methods = Reflect.find(ReflectTest.class, Reflect.METHOD_ITERATOR,
        x -> x.getName().startsWith("findTest"), Reflect.array(Method.class));
    Assert.assertNotNull(methods);
    Assert.assertEquals(1, methods.length);
    Assert.assertEquals(1, methods[0].length);
    Assert.assertEquals(ReflectTest.class.getMethod("findTest"), methods[0][0]);

    Reflect.enable(methods);
    Reflect.enable((AccessibleObject) null);
    Reflect.enable((AccessibleObject[]) null);
    Reflect.enable((AccessibleObject[][]) null);

    Reflect.find(ReflectTest.class, Reflect.METHOD_ITERATOR, x -> x.getName().startsWith("test"),
        Reflect.flat(Method.class), x -> x.toString(), x -> true);
  }

  public static class GetSet {
    private String value;
    private boolean bool;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public boolean isBool() {
      return bool;
    }

    public void setBool(boolean bool) {
      this.bool = bool;
    }
  }

  @Test
  public void isSetterTest() throws NoSuchMethodException, SecurityException {
    Method getValue = GetSet.class.getMethod("getValue");
    Method setValue = GetSet.class.getMethod("setValue", String.class);
    Method setBool = GetSet.class.getMethod("setBool", boolean.class);

    Assert.assertFalse(Reflect.isSetter(getValue));
    Assert.assertTrue(Reflect.isSetter(setValue));
    Assert.assertTrue(Reflect.isSetter(setBool));
  }

  @Test
  public void getSetterTest() throws NoSuchMethodException, SecurityException {
    Method setValue = GetSet.class.getMethod("setValue", String.class);
    Assert.assertEquals(setValue, Reflect.getSetter(GetSet.class, "value", String.class));
    Assert.assertNull(Reflect.getSetter(GetSet.class, "value2", String.class));
  }

  @Test
  public void findMethodTest() throws NoSuchMethodException, SecurityException {
    Method setValue = GetSet.class.getMethod("setValue", String.class);
    Assert.assertEquals(setValue, Reflect.findMethod(GetSet.class, "setValue", 1));
    Assert.assertNull(Reflect.findMethod(GetSet.class, "setValue", 0));
  }

  @Test
  public void getTypeTest() throws NoSuchMethodException, SecurityException, NoSuchFieldException {
    Assert.assertEquals(String.class, Reflect.getType(GetSet.class.getDeclaredField("value")));
    Assert.assertEquals(String.class, Reflect.getType(GetSet.class.getDeclaredMethod("getValue")));
    Assert.assertEquals(GetSet.class, Reflect.getType(GetSet.class.getConstructor()));

    Assert.assertEquals(String.class,
        Reflect.getType(GetSet.class.getDeclaredMethod("setValue", String.class), 0));
    Assert.assertNull(Reflect.getType(GetSet.class.getDeclaredField("value"), 0));
  }

  @Test
  public void isDefinedTest()
      throws NoSuchMethodException, SecurityException, NoSuchFieldException {
    Assert.assertTrue(Reflect.isDefined(String.class.getName()));
    Assert.assertFalse(Reflect.isDefined(String.class.getName() + "2"));
  }

  @Test
  public void existsTest() {
    Assert.assertTrue(Reflect.exists(String.class.getName(), this.getClass().getClassLoader()));
    Assert.assertFalse(
        Reflect.exists(String.class.getName() + "2", this.getClass().getClassLoader()));

    Assert.assertTrue(Reflect.exists(GetSet.class, "getValue", String.class));
    Assert.assertFalse(Reflect.exists(GetSet.class, "getValue", Integer.class));
    Assert.assertFalse(Reflect.exists(GetSet.class, "getValue", String.class, String.class));

    Assert.assertTrue(Reflect.existsMethod(GetSet.class, "setValue", String.class));
    Assert.assertFalse(Reflect.existsMethod(GetSet.class, "setValue", Integer.class));
    Assert.assertFalse(Reflect.existsMethod(GetSet.class, "setValue", String.class, String.class));
  }

  @Test
  public void getMethodTest() throws NoSuchMethodException, SecurityException {
    Method setValue = GetSet.class.getMethod("setValue", String.class);
    Assert.assertEquals(setValue, Reflect.getMethod(GetSet.class, "setValue", 1));
    Assert.assertNull(Reflect.getMethod(GetSet.class, "setValue", 0));
  }

  public static enum EnumTest {
    Val0, Val1, Val2
  }

  @Test
  public void enumValueTest() {
    Assert.assertEquals(EnumTest.Val0, Reflect.enumValue(EnumTest.class, "Val0"));
    try {
      Reflect.enumValue(EnumTest.class, "Val3");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }

    Assert.assertEquals(EnumTest.Val0, Reflect.enumValue(EnumTest.class.getName(), "Val0"));
    try {
      Reflect.enumValue(EnumTest.class.getName(), "Val3");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }

    try {
      Reflect.enumValue(EnumTest.class.getName() + "2", "Val1");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

  public static interface StringList extends List<String> {
  }

  public static interface As {

    List<Long> get();

  }

  @Test
  public void parseAsGenericTest() throws ClassNotFoundException {

    String var = String.class.getName();
    Assert.assertEquals(String.class,
        Reflect.parseAsGeneric(var, this.getClass().getClassLoader(), 0, var.length()));

    var = "java.util.List<" + String.class.getName() + ">";
    Type type = Reflect.parseAsGeneric(var, this.getClass().getClassLoader(), 0, var.length());
    Assert.assertEquals(List.class, Reflect.toClass(type));
    Assert.assertEquals(String.class, Reflect.getTypeArguments(List.class, type, null).get(0));
  }

  @Test
  public void toTypeTest() throws Exception {
    Type iterator = StringList.class.getMethod("iterator").getGenericReturnType();

    Type resolved = Reflect.toType(StringList.class, iterator);

    Assert.assertTrue(resolved instanceof ParameterizedType);
    Assert.assertEquals(String.class,
        Reflect.getTypeArguments(Iterator.class, resolved, null).get(0));
  }

  @Test
  public void toTypeTest2() throws Exception {
    Type generic = As.class.getMethod("get").getGenericReturnType();
    Type iterator = List.class.getMethod("iterator").getGenericReturnType();

    Type resolved = Reflect.toType(generic, iterator);

    Assert.assertTrue(resolved instanceof ParameterizedType);
    Assert.assertEquals(Long.class,
        Reflect.getTypeArguments(Iterator.class, resolved, null).get(0));
  }

}
