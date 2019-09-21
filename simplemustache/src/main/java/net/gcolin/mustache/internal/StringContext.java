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

package net.gcolin.mustache.internal;

import net.gcolin.common.io.StringBuf;
import net.gcolin.mustache.MustacheContext;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class StringContext extends Context {

	public StringContext(MustacheContext templates) {
		super(templates);
	}

	private StringBuf str = new StringBuf();

	@Override
	void write(String val) {
		if (isIndentNext()) {
			str.write(getIndent());
			setIndentNext(false);
		}
		str.write(val);
	}

	@Override
	void write(char val) {
		if (isIndentNext()) {
			str.write(getIndent());
			setIndentNext(false);
		}
		str.write(val);
	}

	public void clear() {
		str.close();
	}

	@Override
	public String toString() {
		return str.toString();
	}

}
