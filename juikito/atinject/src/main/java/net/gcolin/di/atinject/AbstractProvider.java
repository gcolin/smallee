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

import javax.inject.Provider;

import net.gcolin.di.core.InjectException;
import net.gcolin.di.core.Key;

/**
 * An extended Provider.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class AbstractProvider<T> implements Provider<T>, Supplier<T> {

	private InstanceCreator builder;

	private Key key;

	public InstanceCreator getBuilder() {
		if (builder == null) {
			builder = createBuilder();
		}
		return builder;
	}

	public void setBuilder(InstanceCreator builder) {
		this.builder = builder;
	}

	public abstract Environment getEnvironment();

	public abstract Class<? extends T> getResolvedType();

	public abstract Type getResolvedGenericType();

	public abstract Class<T> getType();

	public abstract Type getGenericType();

	public abstract T create();

	public T getNoCreate() {
		return null;
	}

	public abstract Class<? extends Annotation> getScope();

	public abstract void stop();

	public void stop(Object instance) {
		InstanceCreator creator = getBuilder();
		if (creator != null) {
			try {
				creator.destroyInstance(instance);
			} catch (Exception e) {
				throw new InjectException("cannot close provider", e);
			}
		}
	}

	public abstract InstanceCreator createBuilder();

	public Key getKey() {
		return key;
	}

	public void setKey(Key key) {
		this.key = key;
	}
}
