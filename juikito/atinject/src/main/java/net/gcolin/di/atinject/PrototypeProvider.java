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

import javax.inject.Provider;

/**
 * Provider that create a new instance for each get.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class PrototypeProvider<T> extends AbstractProvider<T> implements Provider<T> {

	private Class<T> clazz;
	private Type genericType;
	private Class<? extends T> resolvedClazz;
	private Type resolvedGenericType;
	protected Environment env;

	public PrototypeProvider(Class<T> clazz, Type genericType, Class<? extends T> resolvedClazz,
			Type resolvedGenericType, Environment env) {
		this.clazz = clazz;
		this.resolvedClazz = resolvedClazz;
		this.resolvedGenericType = resolvedGenericType;
		this.env = env;
		this.genericType = genericType;
	}

	@Override
	public Environment getEnvironment() {
		return env;
	}

	@Override
	public T get() {
		return create();
	}

	@SuppressWarnings("unchecked")
	public InstanceCreator createBuilder() {
		return new InstanceBuilder(env, true, (AbstractProvider<Object>) this);
	}

	@Override
	public Class<T> getType() {
		return clazz;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T create() {
		return (T) getBuilder().newInstance().get();
	}

	@Override
	public Type getGenericType() {
		return genericType;
	}

	@Override
	public Class<? extends T> getResolvedType() {
		return resolvedClazz;
	}

	@Override
	public Type getResolvedGenericType() {
		return resolvedGenericType;
	}

	@Override
	public void stop() {
	}

}
