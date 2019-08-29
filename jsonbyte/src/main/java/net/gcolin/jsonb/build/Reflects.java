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

package net.gcolin.jsonb.build;

import net.gcolin.common.reflect.Reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import javax.json.bind.JsonbException;

/**
 * Some reflection utilities.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Reflects {

	private Reflects() {
	}

	/**
	 * Create an instance factory.
	 * 
	 * @param type a type to create
	 * @return a factory
	 */
	public static Function<Object, Object> buildGenerator(Class<?> type) {
		if (type.getEnclosingClass() != null && !Modifier.isStatic(type.getModifiers())) {
			Constructor<?> ctor;
			try {
				ctor = type.getDeclaredConstructor(type.getEnclosingClass());
			} catch (NoSuchMethodException | SecurityException e1) {
				throw new JsonbException(e1.getMessage(), e1);
			}
			return withParent(type, ctor);
		} else if (!Modifier.isPublic(type.getModifiers())) {
			Constructor<?> ctor = null;
			Constructor<?> ctor2 = null;
			try {
				for (Constructor<?> conts : type.getDeclaredConstructors()) {
					if (conts.getParameterCount() == 0) {
						ctor = conts;
					} else if (conts.getParameterCount() == 1
							&& conts.getParameterTypes()[0] == type.getEnclosingClass()) {
						ctor2 = conts;
					}
				}
			} catch (SecurityException e1) {
				throw new JsonbException(e1.getMessage(), e1);
			}
			if (ctor == null) {
				return withParent(type, ctor2);
			} else {
				return withoutParent(ctor);
			}
		} else {
			return parent -> {
				try {
					return type.getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException ex) {
					throw new JsonbException(ex.getMessage(), ex);
				}
			};
		}
	}

	private static Function<Object, Object> withParent(Class<?> type, Constructor<?> ctor) {
		Reflect.enable(ctor);
		return parent -> {
			if (parent == null) {
				parent = buildGenerator(type.getEnclosingClass()).apply(null);
			}
			try {
				return ctor.newInstance(parent);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
				throw new JsonbException(ex.getMessage(), ex);
			}
		};
	}

	private static Function<Object, Object> withoutParent(Constructor<?> ctor) {
		Reflect.enable(ctor);
		return parent -> {
			try {
				return ctor.newInstance();
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
				throw new JsonbException(ex.getMessage(), ex);
			}
		};
	}
	
	public static <T> T newInstance(Class<T> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException ex) {
			throw new JsonbException(ex.getMessage(), ex);
		}
	}

}
