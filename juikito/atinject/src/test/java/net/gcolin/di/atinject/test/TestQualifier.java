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
package net.gcolin.di.atinject.test;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import org.junit.Test;

import net.gcolin.di.atinject.Environment;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TestQualifier {

  @Qualifier
  @Target({TYPE, METHOD, FIELD})
  @Retention(RUNTIME)
  public static @interface TheBest {
  }

  public static interface B {
  }

  @Singleton
  public static class A {
    @Inject
    @TheBest
    B b;
  }

  @TheBest
  public static class B1 implements B {
  }

  public static class B2 implements B {
  }

  @Test
  public void test() throws Exception {
    Environment env = new Environment();
    env.addClasses(A.class, B1.class, B2.class);

    A a = env.get(A.class);

    assertTrue(a.b instanceof B1);
  }
}
