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

package net.gcolin.di.atinject;

/**
 * InstanceCreator adapter.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ForwardInstanceCreator implements InstanceCreator {

	private InstanceCreator delegate;

	public ForwardInstanceCreator(InstanceCreator delegate) {
		this.delegate = delegate;
	}

	public InstanceCreator getDelegate() {
		return delegate;
	}

	public boolean hasDestroyMethods() {
		return delegate.hasDestroyMethods();
	}

	public Runnable createDestroyHandler(Object o) {
		return delegate.createDestroyHandler(o);
	}

	public void destroyInstance(Object o) {
		delegate.destroyInstance(o);
	}

	public Instance newInstance() {
		return delegate.newInstance();
	}

	@Override
	public Instance completeInstance(Instance o) {
		return delegate.completeInstance(o);
	}

	public Instance createInstance(InstanceBuilderMetaData medataData) {
		return delegate.createInstance(medataData);
	}

	public InstanceBuilderMetaData getMetaData() {
		return delegate.getMetaData();
	}

	public void bind(Object o) {
		delegate.bind(o);
	}

	public void bind(Object o, InstanceBuilderMetaData medata) {
		delegate.bind(o, medata);
	}

	@Override
	public AbstractProvider<Object> getProvider() {
		return delegate.getProvider();
	}

}
