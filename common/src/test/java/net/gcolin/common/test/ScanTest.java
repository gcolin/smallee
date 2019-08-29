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

import net.gcolin.common.Logs;
import net.gcolin.common.io.Io;
import net.gcolin.common.reflect.Scan;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;

public class ScanTest {

  @Test
  public void scanBadUrl() throws MalformedURLException {
    try {
      Scan.classes(new URL("asdffg"), x -> {
      }, ScanTest.class.getClassLoader());
      Assert.fail();
    } catch (Exception ex) {
      // ok
    }
  }

  @Test
  public void scanFsBadClassPath() throws MalformedURLException {
    Set<Class<?>> set = new HashSet<>();
    Scan.classes(FileFinder.getTest().toURI().toURL(), x -> set.add(x),
        AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
          public ClassLoader run() {
            return new URLClassLoader(new URL[0], null);
          }
        }));
    Assert.assertTrue(set.isEmpty());
  }

  @Test
  public void scanFs() throws MalformedURLException {
    Set<Class<?>> set = new HashSet<>();
    Scan.classes(FileFinder.getTest().toURI().toURL(), x -> set.add(x),
        ScanTest.class.getClassLoader());
    Assert.assertFalse(set.isEmpty());
    Assert.assertTrue(set.contains(ScanTest.class));
  }

  @Test
  public void scanJarBadClassPath() throws IOException {
    File dir = FileFinder.getTest();
    File tmpzip = new File(dir.getParentFile(), "test-classes.jar");
    Io.zip(tmpzip, dir);

    Set<Class<?>> set = new HashSet<>();
    Scan.classes(tmpzip.toURI().toURL(), x -> set.add(x),
        AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
          public ClassLoader run() {
            return new URLClassLoader(new URL[0], null);
          }
        }));
    Assert.assertTrue(set.isEmpty());

    if (!tmpzip.delete()) {
      Logs.LOG.warning("cannot delete " + tmpzip);
    }
  }

  @Test
  public void scanJar() throws IOException {
    File dir = FileFinder.getTest();
    File tmpzip = new File(dir.getParentFile(), "test-classes.jar");
    Io.zip(tmpzip, dir);

    Set<Class<?>> set = new HashSet<>();
    Scan.classes(tmpzip.toURI().toURL(), x -> set.add(x), ScanTest.class.getClassLoader());
    Assert.assertFalse(set.isEmpty());
    Assert.assertTrue(set.contains(ScanTest.class));

    if (!tmpzip.delete()) {
      Logs.LOG.warning("cannot delete " + tmpzip);
    }
  }

  @Test
  public void scanResourceInJar() throws IOException {
    File dir = FileFinder.getTestResources();
    File tmpzip = new File(dir.getParentFile(), "test-classes.jar");
    Io.zip(tmpzip, dir);

    Set<String> set = new HashSet<>();
    Scan.resources(tmpzip.toURI().toURL(), (path, url) -> {
      if (path.equals("lipsum.txt")) {
        set.add(path);
      }
    });
    Assert.assertFalse(set.isEmpty());
    Assert.assertEquals(1, set.size());
    if (!tmpzip.delete()) {
      Logs.LOG.warning("cannot delete " + tmpzip);
    }
  }

  @Test
  public void scanResourceInFs() throws IOException {
    Set<String> set = new HashSet<>();
    Scan.resources(FileFinder.getTestResources().toURI().toURL(), (path, url) -> {
      if (path.equals("lipsum.txt")) {
        set.add(path);
      }
    });
    Assert.assertFalse(set.isEmpty());
    Assert.assertEquals(1, set.size());
  }
}
