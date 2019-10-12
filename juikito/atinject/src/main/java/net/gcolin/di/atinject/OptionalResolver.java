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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.logging.Level;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.core.NotFoundException;

/**
 * Resolve an Optional.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class OptionalResolver implements Resolver {

	private Environment environment;

	public OptionalResolver(Environment environment) {
		this.environment = environment;
	}

	protected Class<?> optionalType() {
		return Optional.class;
	}

	protected Object of(Object val) {
		return Optional.of(val);
	}

	protected Object empty() {
		return Optional.empty();
	}

	protected Type getType(Type genericType) {
		return ((ParameterizedType) genericType).getActualTypeArguments()[0];
	}

	protected Class<?> getPrimitiveType() {
		return null;
	}

	@Override
	public AbstractProvider<Object> find(Class<?> clazz, Type genericType, Annotation[] qualifiers) {
		if (clazz == optionalType()) {
			Type type = getType(genericType);
			try {
				@SuppressWarnings("unchecked")
				AbstractProvider<Object> delegate = (AbstractProvider<Object>) environment
						.getProvider(Reflect.toClass(type), type, qualifiers);
				return new SingletonProvider<Object>(of(delegate.get()), genericType);
			} catch (NotFoundException ex) {
				if (getPrimitiveType() != null) {
					try {
						@SuppressWarnings("unchecked")
						AbstractProvider<Object> delegate = (AbstractProvider<Object>) environment
								.getProvider(getPrimitiveType(), getPrimitiveType(), qualifiers);
						return new SingletonProvider<Object>(of(delegate.get()), genericType);
					} catch (NotFoundException ex2) {
						environment.getLog().log(Level.FINE, "cannot find", ex2);
					}
				}
			}
			return new SingletonProvider<Object>(empty(), genericType);
		}
		return null;
	}

}
