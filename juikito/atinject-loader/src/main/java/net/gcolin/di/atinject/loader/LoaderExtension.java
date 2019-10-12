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
package net.gcolin.di.atinject.loader;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;

import net.gcolin.common.io.Io;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.core.InjectException;

/**
 * Load classes from the file "META-INF/atinject".
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class LoaderExtension implements Extension {

	@Override
	public void doStart(Environment env) {
		try {
			Enumeration<URL> urls = env.getClassLoader().getResources("META-INF/atinject");
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				env.add(Arrays.stream(Io.readLines(url, Class.class, name -> {
					try {
						return env.getClassLoader().loadClass(name);
					} catch (ClassNotFoundException ex) {
						env.getLog().log(Level.WARNING, ex.getMessage(), ex);
						return null;
					}
				})).filter(x -> x != null).toArray(n -> new Class[n]));
			}
		} catch (IOException ex) {
			throw new InjectException(ex);
		}
	}

}
