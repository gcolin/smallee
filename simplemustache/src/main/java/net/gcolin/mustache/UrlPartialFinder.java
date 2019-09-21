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

package net.gcolin.mustache;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import net.gcolin.common.io.Io;
import net.gcolin.common.io.StringWriter;

/**
 * Partial Finder by URL.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class UrlPartialFinder extends AbstractPartialFinder {

	private UrlFunction fun;
	private String prefix;

	public UrlPartialFinder(String extension, UrlFunction fun, String prefix) {
		super(extension);
		this.fun = fun;
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

	@Override
	protected void load() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String get(Object key) {
		String val = super.get(key);
		if (val == null) {
			URL url = fun.get(getPrefix() + key + getExtension());
			if (url != null) {
				try (Reader reader = Io.reader(url.openStream(), "utf-8")) {
					StringWriter sw = new StringWriter();
					Io.copy(reader, sw);
					val = sw.toString();
					put((String) key, val);
					sw.close();
				} catch (IOException ex) {
					throw new MustacheException(ex);
				}
			}
		}
		return val;
	}

}
