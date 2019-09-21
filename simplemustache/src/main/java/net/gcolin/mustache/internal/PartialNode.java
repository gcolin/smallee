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
public class PartialNode extends Node {

	private String name;
	private String indent;

	public PartialNode(String name, String indent) {
		this.name = name;
		this.indent = indent;
	}

	@Override
	void write(Context ctx) {
		ctx.goDeeper();
		String oldIndent = ctx.getIndent();
		ctx.setIndent(indent);
		ctx.getMustacheContext().get(name).write(ctx);
		ctx.setIndent(oldIndent);
		ctx.goUpper();
	}

	@Override
	public String toString() {
		return "{{>" + name + "}}";
	}

}
