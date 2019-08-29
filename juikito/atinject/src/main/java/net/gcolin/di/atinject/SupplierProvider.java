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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * A provider from a supplier.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class SupplierProvider<T> extends AbstractProvider<T> implements InstanceCreator {

	private Supplier<T> supplier;
	private Class<T> type;
	private Class<? extends Annotation> scope;
	private Environment env;
	private static final InstanceBuilderMetaData META_DATA = new InstanceBuilderMetaData();

	public SupplierProvider(Class<T> type, Supplier<T> supplier, Class<? extends Annotation> scope, Environment env) {
		this.type = type;
		this.supplier = supplier;
		this.scope = scope;
		this.env = env;
	}

	@Override
	public T get() {
		return create();
	}

	@Override
	public InstanceCreator getBuilder() {
		return this;
	}

	@Override
	public Class<? extends T> getResolvedType() {
		return type;
	}

	@Override
	public Type getResolvedGenericType() {
		return type;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public Type getGenericType() {
		return type;
	}

	@Override
	public T create() {
		return supplier.get();
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return scope;
	}

	@Override
	public InstanceCreator createBuilder() {
		return this;
	}

	@Override
	public void stop() {
	}

	@Override
	public Environment getEnvironment() {
		return env;
	}

	@Override
	public boolean hasDestroyMethods() {
		return false;
	}

	@Override
	public Runnable createDestroyHandler(final Object o) {
		return () -> destroyInstance(o);
	}

	@Override
	public void destroyInstance(Object o) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public Instance newInstance() {
		return completeInstance(new Instance(create(), (AbstractProvider<Object>) this));
	}

	@Override
	public Instance completeInstance(Instance o) {
		return o;
	}

	@Override
	public Instance createInstance(InstanceBuilderMetaData medataData) {
		return newInstance();
	}

	@Override
	public InstanceBuilderMetaData getMetaData() {
		return META_DATA;
	}

	@Override
	public void bind(Object o) {
	}

	@Override
	public void bind(Object o, InstanceBuilderMetaData medata) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public AbstractProvider<Object> getProvider() {
		return (AbstractProvider<Object>) this;
	}

}
