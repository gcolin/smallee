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

import java.util.Map;

import net.gcolin.common.collection.ArrayQueue;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class TransientStringContext extends StringContext {

	private Context delegate;

	public TransientStringContext(Context delegate) {
		super(delegate.getMustacheContext());
		this.delegate = delegate;
	}
	
	@Override
	public Map<String, String> getDefines() {
		return delegate.getDefines();
	}

	@Override
	public Object scope() {
		return delegate.scope();
	}

	@Override
	public void inScope(Object obj) {
		delegate.inScope(obj);
	}

	@Override
	public void inScope(Object obj, int index, boolean first, boolean last) {
		delegate.inScope(obj, index, first, last);
	}

	@Override
	public void outScope() {
		delegate.outScope();
	}

	@Override
	public void goDeeper() {
		delegate.goDeeper();
	}

	@Override
	public void goUpper() {
		delegate.goUpper();
	}

	@Override
	public int getDepth() {
		return delegate.getDepth();
	}

	@Override
	public ArrayQueue<Scope> getScopes() {
		return delegate.getScopes();
	}
}
