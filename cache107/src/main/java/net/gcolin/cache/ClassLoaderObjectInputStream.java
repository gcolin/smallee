/*
 * Copyright 2011-2013 Terracotta, Inc. Copyright 2011-2013 Oracle America Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Taken from the RI.
 *
 * @since 1.0
 */
public class ClassLoaderObjectInputStream extends ObjectInputStream {

  /**
   * The {@link ClassLoader} to use.
   */
  private final ClassLoader classloader;

  /**
   * Create a ClassLoaderObjectInputStream.
   *
   * @param in the {@link InputStream}
   * @param classloader the {@link ClassLoader}
   * @throws IOException should the stream not be created
   */
  public ClassLoaderObjectInputStream(InputStream in, ClassLoader classloader) throws IOException {
    super(in);
    this.classloader = classloader;
  }

  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc)
      throws IOException, ClassNotFoundException {
    String name = desc.getName();
    try {
      return Class.forName(name, false, classloader);
    } catch (ClassNotFoundException ex) {
      CachingProviderImpl.LOGGER.debug(ex.getMessage(), ex);
      return super.resolveClass(desc);
    }
  }

}
