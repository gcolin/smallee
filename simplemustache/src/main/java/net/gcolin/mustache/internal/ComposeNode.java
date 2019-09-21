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
 * Mustache compose node.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ComposeNode extends Node {

	private String name;
	private Node body;

	public ComposeNode(String name, Node body) {
		this.name = name;
		this.body = body;
	}

	@Override
	void write(Context ctx) {
		int nb = 0;
		while (ctx.getDepth() >= 0) {
			ctx.goUpper();
			nb++;
		}
		if (body != null) {
			body.write(ctx);
		}
		while (nb > 0) {
			ctx.goDeeper();
			nb--;
		}
		ctx.getMustacheContext().get(name).write(ctx);
	}

	@Override
	public String toString() {
		return "{{<" + name + "}}";
	}
}
