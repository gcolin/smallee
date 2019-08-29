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

package net.gcolin.common.reflect;

import net.gcolin.common.io.Io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A jar ScanProvider
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JarProvider implements ScanProvider {

  @Override
  public void find(URL url, BiConsumer<String, Supplier<URL>> consumer) throws IOException {
    if ("jar".equals(url.getProtocol())) {
      url = new URL(Scan.toRootPath(url.toString()));
    }

    URL zipurl = url;

    try (ZipInputStream zip = new ZipInputStream(url.openStream())) {
      for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
        if (!entry.isDirectory()) {
          String name = entry.getName();
          consumer.accept(name, () -> {
            try {
              return new URL(zipurl, zipurl.toString() + "!/" + name, new URLStreamHandler() {

                @Override
                protected URLConnection openConnection(URL url) throws IOException {
                  return new URLConnection(url) {

                    byte data[];
                    
                    @Override
                    public void connect() throws IOException {}

                    @Override
                    public InputStream getInputStream() throws IOException {
                      if(data == null) {
                        data = Io.toByteArray(zip);
                      }
                      return new ByteArrayInputStream(data);
                    }
                  };
                }
              });
            } catch (MalformedURLException ex) {
              throw new ScanException(ex);
            }

          });

        }
      }
    }
  }

  @Override
  public boolean accept(URL url) {
    return "jar".equals(url.getProtocol()) || url.getPath().endsWith(".jar");
  }
}
