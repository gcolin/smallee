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

package net.gcolin.mustache;

import net.gcolin.mustache.internal.CompiledMustacheImpl;
import net.gcolin.mustache.internal.Compiler;
import net.gcolin.mustache.internal.MapScope;
import net.gcolin.mustache.internal.Node;
import net.gcolin.mustache.internal.ObjectScope;
import net.gcolin.mustache.internal.Scope;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.gcolin.mustache.internal.DomScope;
import net.gcolin.mustache.internal.Getter;

/**
 * Mustache context.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MustacheContext {

	private Map<Class<?>, Map<String, Getter>> bindingCache = new ConcurrentHashMap<>();

	private Map<String, Node> db = new ConcurrentHashMap<>();
	private Map<String, String> partials;

	/**
	 * Create a MustacheContext.
	 * 
	 * @param partials partials by name
	 */
	@SuppressWarnings("unchecked")
	public MustacheContext(Map<String, String> partials) {
		this.partials = partials;
		if (partials == null) {
			this.partials = Collections.EMPTY_MAP;
		}
	}

	public CompiledMustache getTemplate(String path) {
		return new CompiledMustacheImpl(this, get(path));
	}

	/**
	 * Create a scope from an Object.
	 * 
	 * @param obj object
	 * @return a Scope
	 */
	public Scope createScope(Object obj) {
		if (obj instanceof Map) {
			return new MapScope(obj);
		} else if (obj instanceof org.w3c.dom.Node) {
			return new DomScope((org.w3c.dom.Node) obj);
		} else {
			Map<String, Getter> bindings = bindingCache.get(obj.getClass());
			if (bindings == null) {
				bindings = new HashMap<>();
				Class<?> clazz = obj.getClass();
				while (clazz != Object.class) {
					for (Method m : clazz.getDeclaredMethods()) {
						if (!Modifier.isPrivate(m.getModifiers()) && m.getParameterTypes().length == 0
								&& m.getReturnType() != Void.TYPE) {
							String name = m.getName();
							String name2 = null;
							if (name.startsWith("get") && name.length() > 3) {
								name2 = Character.toLowerCase(name.charAt(3)) + name.substring(4);
							} else if (name.startsWith("is") && name.length() > 2) {
								name2 = Character.toLowerCase(name.charAt(2)) + name.substring(3);
							}
							MethodGetter getter = null;
							if (!bindings.containsKey(name)) {
								bindings.put(name, getter = new MethodGetter(m));
							}
							if (name2 != null && !bindings.containsKey(name2)) {
								if (getter == null) {
									getter = new MethodGetter(m);
								}
								bindings.put(name2, getter);
							}
						}
					}

					for (Field field : clazz.getDeclaredFields()) {
						if (!Modifier.isPrivate(field.getModifiers())) {
							String name = field.getName();
							if (!bindings.containsKey(name)) {
								bindings.put(name, new FieldGetter(field));
							}
						}
					}

					clazz = clazz.getSuperclass();
				}
				bindingCache.put(obj.getClass(), bindings);
			}
			return new ObjectScope(obj, bindings);
		}
	}

	private static class MethodGetter implements Getter {

		private Method method;

		MethodGetter(Method method) {
			this.method = method;
			method.setAccessible(true);
		}

		@Override
		public Object get(Object obj) {
			try {
				return method.invoke(obj);
			} catch (IllegalAccessException | InvocationTargetException ex) {
				throw new MustacheException(ex);
			}
		}

	}

	private static class FieldGetter implements Getter {

		private Field field;

		FieldGetter(Field field) {
			this.field = field;
			field.setAccessible(true);
		}

		@Override
		public Object get(Object obj) {
			try {
				return field.get(obj);
			} catch (IllegalAccessException ex) {
				throw new MustacheException(ex);
			}
		}

	}

	public Node put(String key, Node value) {
		return db.put(key, value);
	}

	public void clear() {
		db.clear();
		bindingCache.clear();
	}

	public boolean remove(Object key, Object value) {
		return db.remove(key) != null;
	}

	public boolean has(String name) {
		return partials.containsKey(name);
	}

	/**
	 * Get a partial.
	 * 
	 * @param name partial name
	 * @return a compiled partial
	 */
	public Node get(String name) {
		Node node = db.get(name);
		if (node == null) {
			String template = partials.get(name);
			if (template == null) {
				throw new MustacheException("Cannot find partial with path " + name);
			}
			node = Compiler.compile(template, this);
			db.put(name, node);
		}
		return node;
	}
}
