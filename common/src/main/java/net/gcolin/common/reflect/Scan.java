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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.gcolin.common.collection.Collections2;

/**
 * Utility class for scanning classes.
 * 
 * <p>
 * For an example, see ScanTest
 * </p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Scan {

	private static final ScanProvider[] provider = Collections2
			.safeFillServiceLoaderAsArray(ScanProvider.class.getClassLoader(), ScanProvider.class);
	private static final String JAR_FILE_SEP = "!/";
	public static final String CLASS = ".class";
	private static final String JAR_PROTOCOL = "jar:";
	private static final String FILE_PROTOCOL = "file:";

	private Scan() {
	}

	/**
	 * Transform a URL of a file in a jar to a the URL of the jar Used by
	 * JarProvider.
	 * 
	 * @param url the URL of a file in a jar
	 * @return the URL of the containing jar
	 */
	public static String toRootPath(String url) {
		String path = url;
		if (path.contains(JAR_FILE_SEP)) {
			path = path.substring(0, path.indexOf(JAR_FILE_SEP));
		}
		if (path.startsWith(JAR_PROTOCOL)) {
			path = path.substring(JAR_PROTOCOL.length());
		}
		if (path.startsWith(FILE_PROTOCOL)) {
			path = path.substring(FILE_PROTOCOL.length());
		}
		return FILE_PROTOCOL + path;
	}

	/**
	 * Scan classes.
	 * 
	 * @param url         the URL of the root path to scan or the URL of a file in a
	 *                    jar
	 * @param consumer    The class analyzer
	 * @param classLoader the class loader of the scanning classes
	 */
	public static void classes(URL url, Consumer<Class<?>> consumer, ClassLoader classLoader) {
		resources(url, (path, uprovider) -> {
			if (path.endsWith(CLASS)) {
				String name = Scan.toClassName(path);
				try {
					consumer.accept(classLoader.loadClass(name));
				} catch (Throwable ex) {
					Logger logger = Logger.getLogger(Scan.class.getName());
					if (logger.isLoggable(Level.FINE)) {
						logger.log(Level.FINE, "cannot load class : " + name, ex);
					}
				}
			}
		});
	}

	/**
	 * Scan classes.
	 * 
	 * @param consumer    The class analyzer
	 * @param classLoader the class loader of the scanning classes
	 */
	public static void classes(Consumer<Class<?>> consumer, ClassLoader classLoader) {
		if (classLoader instanceof URLClassLoader) {
			for (URL url : ((URLClassLoader) classLoader).getURLs()) {
				classes(url, consumer, classLoader);
			}
		}
	}

	/**
	 * Scan classes.
	 * 
	 * @param url      the URL to explore.
	 * @param consumer The class analyzer
	 */
	public static void resources(URL url, BiConsumer<String, Supplier<URL>> consumer) {
		for (int i = 0; i < provider.length; i++) {
			if (provider[i].accept(url)) {
				try {
					provider[i].find(url, consumer);
				} catch (IOException ex) {
					throw new ScanException(ex);
				}
				break;
			}
		}
	}

	public static String toClassName(String entry) {
		return entry.substring(0, entry.length() - CLASS.length()).replace('/', '.').replace(File.separatorChar, '.');
	}

}
