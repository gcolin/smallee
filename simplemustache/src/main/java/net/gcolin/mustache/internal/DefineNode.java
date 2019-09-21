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
 * Mustache define node.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DefineNode extends Node {

	private String name;
	private Node body;
	private String indent;

	/**
	 * Create a define node.
	 * 
	 * @param name   name
	 * @param body   body
	 * @param indent indent
	 */
	public DefineNode(String name, Node body, String indent) {
		this.name = name;
		this.body = body;
		this.indent = indent;
	}

	@Override
	void write(Context ctx) {
		if (body == null) {
			String content = ctx.getDefines().get(name);
			if (content != null) {
				int prec = 0;
				while (prec < content.length()) {
					int split = content.indexOf('\n', prec);
					if (split == -1) {
						split = content.length();
						ctx.write(content.substring(prec));
					} else if (split == content.length() - 1) {
						ctx.write(content.substring(prec, split));
						ctx.write('\n');
						split++;
					} else {
						ctx.write(content.substring(prec, ++split));
						ctx.write(indent);
					}
					prec = split;
				}
			}
		} else if (!ctx.getDefines().containsKey(name) && body != null) {
			TransientStringContext cc = new TransientStringContext(ctx);
			body.write(cc);
			String out = cc.toString();
			cc.clear();
			ctx.getDefines().put(name, out);
		}
	}

}
