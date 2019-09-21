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

import net.gcolin.mustache.CompiledMustache;
import net.gcolin.mustache.MustacheContext;

import java.io.Writer;

/**
 * Implementation of CompiledMustache.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CompiledMustacheImpl implements CompiledMustache {

	private Node node;
	private MustacheContext templates;

	/**
	 * Create a CompiledMustacheImpl.
	 * 
	 * @param templates partials
	 * @param node      root node
	 */
	public CompiledMustacheImpl(MustacheContext templates, Node node) {
		this.templates = templates;
		this.node = node;
	}

	@Override
	public String render(Object scope) {
		StringContext ctx = new StringContext(templates);
		if (scope != null) {
			ctx.inScope(scope);
		}
		node.write(ctx);
		return ctx.toString();
	}

	@Override
	public void render(Object scope, Writer writer) {
		WriterContext ctx = new WriterContext(templates, writer);
		if (scope != null) {
			ctx.inScope(scope);
		}
		node.write(ctx);
	}

}
