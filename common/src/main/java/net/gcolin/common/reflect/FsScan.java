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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * A file system ScanProvider.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class FsScan implements ScanProvider {

  @Override
  public void find(URL url, BiConsumer<String, Supplier<URL>> consumer) {
    try {
      String rootPath;
      File file = new File(url.toURI());
      if (file.exists()) {
        rootPath = file.getAbsolutePath();
        Path path = file.toPath();
        Files.walkFileTree(path, new Visitor(consumer, rootPath));
        if (path.endsWith("test") && path.getParent().endsWith("resources")
            && path.getParent().getParent().endsWith("build")) {
          // gradle test
          path = path.getParent().getParent().resolve("classes/test");
          rootPath = path.toString();
          Files.walkFileTree(path, new Visitor(consumer, rootPath));
        }
        if (path.endsWith("main") && path.getParent().endsWith("resources")
            && path.getParent().getParent().endsWith("build")) {
          // gradle main
          path = path.getParent().getParent().resolve("classes/main");
          rootPath = path.toString();
          if(Files.exists(path)) {
            Files.walkFileTree(path, new Visitor(consumer, rootPath));
          }
        }
      }
    } catch (URISyntaxException | IOException ex) {
      throw new ScanException(ex);
    }

  }

  @Override
  public boolean accept(URL url) {
    return "file".equals(url.getProtocol()) && !url.getPath().endsWith(".jar");
  }

  private static class Visitor extends SimpleFileVisitor<Path> {
    private BiConsumer<String, Supplier<URL>> consumer;
    private String rootPath;

    public Visitor(BiConsumer<String, Supplier<URL>> consumer, String rootPath) {
      this.consumer = consumer;
      this.rootPath = rootPath;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      String fileName = file.toFile().getAbsolutePath();
      fileName = fileName.substring(rootPath.length() + 1);
      consumer.accept(fileName, () -> {
        try {
          return file.toUri().toURL();
        } catch (MalformedURLException ex) {
          throw new ScanException(ex);
        }
      });
      return super.visitFile(file, attrs);
    }
  }

}
