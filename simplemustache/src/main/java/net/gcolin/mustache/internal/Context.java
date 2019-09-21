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

import java.util.HashMap;
import java.util.Map;

import net.gcolin.common.collection.ArrayQueue;
import net.gcolin.mustache.MustacheContext;
import net.gcolin.mustache.MustacheException;

/**
 * Runtime context.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class Context {

	private ArrayQueue<Scope> scopes = new ArrayQueue<>();
	private MustacheContext templates;
	private int depth = 0;
	private String indent = "";
	private boolean indentNext;
	private Map<String, String> defines;

	public Context(MustacheContext templates) {
		this.templates = templates;
	}

	/**
	 * Get defines.
	 * 
	 * @return defines store
	 */
	public Map<String, String> getDefines() {
		if (defines == null) {
			defines = new HashMap<>();
		}
		return defines;
	}

	public MustacheContext getMustacheContext() {
		return templates;
	}

	abstract void write(String str);

	abstract void write(char str);

	/**
	 * Indicate a partial in.
	 */
	public void goDeeper() {
		if (++depth == 100) {
			throw new MustacheException("Maximum partial recursion limit reached: 100");
		}
	}

	/**
	 * Indicate a partial out.
	 */
	public void goUpper() {
		if (--depth == -100) {
			throw new MustacheException("Maximum partial recursion limit reached: -100");
		}
	}

	public int getDepth() {
		return depth;
	}

	public Object scope() {
		return scopes.peek().get();
	}

	/**
	 * Enter into a new scope.
	 * 
	 * @param obj the instance of the scope
	 */
	public void inScope(Object obj) {
		scopes.offer(getMustacheContext().createScope(obj));
	}

	public void inScope(Object obj, int index, boolean first, boolean last) {
		scopes.offer(new SectionScope(getMustacheContext().createScope(obj), index, first, last));
	}

	public void outScope() {
		scopes.poll();
	}

	public ArrayQueue<Scope> getScopes() {
		return scopes;
	}

	/**
	 * Get a value from a name.
	 * 
	 * @param name name
	 * @return a value
	 */
	public Object get(String name) {
		ArrayQueue<Scope> scopes = getScopes();
		for (int i = scopes.size() - 1; i >= 0; i--) {
			if (scopes.get(i).has(name)) {
				return scopes.get(i).get(name);
			}
		}
		return null;
	}

	/**
	 * Get a value from a path of names.
	 * 
	 * @param names path
	 * @return a value
	 */
	public Object get(String[] names) {
		Object obj = get(names[0]);
		if (obj == null) {
			return null;
		}
		MustacheContext ctx = getMustacheContext();
		Scope scope;
		for (int i = 1; obj != null && i < names.length; i++) {
			scope = ctx.createScope(obj);
			if (scope.has(names[i])) {
				obj = scope.get(names[i]);
			} else {
				obj = null;
			}
		}
		return obj;
	}

	public String getIndent() {
		return indent;
	}

	public void setIndent(String indent) {
		this.indent = indent;
	}

	public boolean isIndentNext() {
		return indentNext;
	}

	public void setIndentNext(boolean indentNext) {
		this.indentNext = indentNext;
	}

}
