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

import java.io.File;

public class FileFinder {

  /**
   * Find the build folder.
   * 
   * @return the build folder.
   */
  public static File getBuild() {
    File file = new File("target");
    if (file.exists()) {
      // maven
      return file;
    }
    // gradle
    return new File("build");
  }

  /**
   * Find the test classes folder.
   * 
   * @return the test classes folder.
   */
  public static File getTest() {
    File file = new File("target/test-classes");
    if (file.exists()) {
      // maven
      return file;
    }
    // gradle
    return new File("build/classes/test");
  }

  /**
   * Find the main classes folder.
   * 
   * @return the main classes folder.
   */
  public static File getMain() {
    File file = new File("target/classes");
    if (file.exists()) {
      // maven
      return file;
    }
    // gradle
    return new File("build/classes/main");
  }

  /**
   * Find the test resources folder.
   * 
   * @return the test resources folder.
   */
  public static File getTestResources() {
    File file = new File("target/test-classes");
    if (file.exists()) {
      // maven
      return file;
    }
    // gradle
    return new File("build/resources/test");
  }

}
