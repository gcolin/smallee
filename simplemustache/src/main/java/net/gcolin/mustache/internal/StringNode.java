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

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class StringNode extends Node {

	private String txt;

	public StringNode(String txt) {
		super();
		this.txt = txt;
	}

	@Override
	public void write(Context ctx) {
		if (!ctx.getIndent().isEmpty()) {
			int prec = 0;
			int index;
			while ((index = txt.indexOf('\n', prec)) != -1) {
				ctx.write(txt.substring(prec, index + 1));
				if (index < txt.length() - 1) {
					ctx.write(ctx.getIndent());
				} else {
					ctx.setIndentNext(true);
				}
				prec = index + 1;
			}
			if (prec != txt.length()) {
				ctx.write(txt.substring(prec));
			}
		} else {
			ctx.write(txt);
		}
	}

	@Override
	public String toString() {
		return txt;
	}

	public void setString(String str) {
		this.txt = str;
	}
}
