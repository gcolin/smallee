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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.core.InjectException;

/**
 * Create an instance with a constructor with the Inject annotation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InjectInstanceFactory implements InstanceFactory {

	private Constructor<?> constructor;
	private AbstractProvider<?>[] providers;
	private AbstractProvider<Object> provider;

	public InjectInstanceFactory(Constructor<?> constructor, AbstractProvider<?>[] providers,
			AbstractProvider<Object> provider) {
		this.constructor = constructor;
		this.providers = providers;
		this.provider = provider;
		Reflect.enable(constructor);
	}

	public Constructor<?> getConstructor() {
		return constructor;
	}

	@Override
	public Instance create() {
		return create(getArguments());
	}

	@Override
	public Instance create(Object[] arguments) {
		try {
			Object obj = constructor.newInstance(arguments);
			return new Instance(obj, provider);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException ex) {
			throw new InjectException("cannot create " + constructor.getDeclaringClass(), ex);
		}
	}

	@Override
	public Object[] getArguments() {
		return InstanceBuilder.getArguments(providers);
	}

}
