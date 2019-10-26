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

import net.gcolin.mustache.StringFunction;
import net.gcolin.mustache.WrapperFunction;
import net.gcolin.mustache.internal.Compiler.CompileContext;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class BiFunctionWrapper {

	private WrapperFunction cache;
	private String previous;
	private Node pnode;
	private CompileContext context;
	private String inner;

	/**
	 * Create a BiFunctionWrapper.
	 *
	 * @param body    inner node
	 * @param cache   bindings
	 * @param context compile context
	 */
	public BiFunctionWrapper(Node body, WrapperFunction cache, CompileContext context) {
		this.inner = body == null ? "" : body.toString();
		this.cache = cache;
		this.context = context;
	}

	public Object get() {
		return cache;
	}

	/**
	 * Execute a wrapper function.
	 *
	 * @param ctx runtime context
	 * @param obj object to wrap
	 * @return wrapped result
	 */
	public Object get(final Context ctx, Object obj) {
		StringFunction fun = new StringFunction() {
			@Override
			public String apply(String name) {
				synchronized (this) {
					if (previous == null || !previous.equals(name)) {
						pnode = Compiler.compile(name, ctx.getMustacheContext(), context);
						previous = name;
					}
				}
				TransientStringContext cc = new TransientStringContext(ctx);
				pnode.write(cc);
				String out = cc.toString();
				cc.clear();
				return out;
			}
		};

		Object result = cache.apply(inner, fun);
		if (result != null) {
			if (result instanceof String && ((String) result).contains(context.em)) {
				Node node = Compiler.compile((String) result, ctx.getMustacheContext(), context);
				TransientStringContext cc = new TransientStringContext(ctx);
				node.write(cc);
				String out = cc.toString();
				cc.clear();
				return out;
			} else {
				return String.valueOf(result);
			}
		}
		return result;
	}
}
