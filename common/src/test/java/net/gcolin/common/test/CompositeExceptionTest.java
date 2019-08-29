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

import net.gcolin.common.io.StringWriter;
import net.gcolin.common.lang.CompositeException;

import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class CompositeExceptionTest {

  @Test
  public void testMessage() {
    CompositeException ex = new CompositeException(
        Arrays.asList(new RuntimeException("hello"), new Exception("world")));

    Assert.assertEquals("hello, world", ex.getMessage());
    Assert.assertEquals("hello, world", ex.getLocalizedMessage());
  }

  @Test
  public void testPrintStackTrace() {
    Exception e1 = new RuntimeException("hello");
    
    StringBuilder str = new StringBuilder();
    StringWriter sw = new StringWriter();
    PrintWriter ps = new PrintWriter(sw);
    e1.printStackTrace(ps);
    str.append(sw.toString());
    sw.close();
    sw = new StringWriter();
    ps = new PrintWriter(sw);
    Exception e2 = new Exception("world");
    e2.printStackTrace(ps);
    str.append(sw.toString());
    sw.close();

    sw = new StringWriter();
    ps = new PrintWriter(sw);
    
    CompositeException ex = new CompositeException(Arrays.asList(e1, e2));
    ex.printStackTrace(ps);

    Assert.assertEquals(str.toString(), sw.toString());
    sw.close();
  }

  @Test
  public void testGetCause() {
    Exception e1 = new RuntimeException("hello");
    Exception e2 = new Exception("world");
    CompositeException ex = new CompositeException(Arrays.asList(e1, e2));
    Assert.assertEquals(e1, ex.getCause());
  }

  @Test
  public void testStack() {
    CompositeException ex = new CompositeException(
        Arrays.asList(new RuntimeException("hello"), new Exception("world")));
    try {
      ex.getStackTrace();
      Assert.fail();
    } catch (UnsupportedOperationException ex2) {
      // ok
    }

    try {
      ex.setStackTrace(null);
      Assert.fail();
    } catch (UnsupportedOperationException ex2) {
      // ok
    }

    try {
      ex.initCause(null);
      Assert.fail();
    } catch (UnsupportedOperationException ex2) {
      // ok
    }
  }

  @Test
  public void testBadArgument() {
    try {
      new CompositeException(null);
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }

    try {
      new CompositeException(new ArrayList<>());
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      // ok
    }
  }

}
