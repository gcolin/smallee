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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.common.Priority;
import net.gcolin.common.reflect.Priorities;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.2
 */
public class PriorityTest {

  public static class A {

    void hello() {}

    @Priority(10)
    void hello2() {}

    @Priority(100)
    void hello3() {}

    @Priority(1)
    void hello4() {}

    @Priority(1000)
    void hello5() {}

  }

  @Test
  public void testArray() {

    Method[] methods = getMethods();
    
    Priorities.sortArray(methods, x -> x.getAnnotation(Priority.class));
    
    Assert.assertEquals("hello4", methods[0].getName());
    Assert.assertEquals("hello2", methods[1].getName());
    Assert.assertEquals("hello3", methods[2].getName());
    Assert.assertEquals("hello5", methods[3].getName());
    Assert.assertEquals("hello", methods[4].getName());
    
    List<Method> list = new ArrayList<>(Arrays.asList(getMethods()));
    
    Priorities.sort(list, x -> x.getAnnotation(Priority.class));
    
    Assert.assertEquals("hello4", list.get(0).getName());
    Assert.assertEquals("hello2", list.get(1).getName());
    Assert.assertEquals("hello3", list.get(2).getName());
    Assert.assertEquals("hello5", list.get(3).getName());
    Assert.assertEquals("hello", list.get(4).getName());

  }

  private Method[] getMethods() {
    return Arrays.stream(A.class.getDeclaredMethods()).filter(x -> x.getName().startsWith("hello"))
        .toArray(x -> new Method[x]);
  }

}
