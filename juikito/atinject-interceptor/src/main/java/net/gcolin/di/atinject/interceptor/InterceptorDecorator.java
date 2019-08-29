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
package net.gcolin.di.atinject.interceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.Interceptors;

import net.gcolin.common.collection.Func;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.DecoratorBuilder;

/**
 * Decorate an interface with interceptor if needed.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InterceptorDecorator implements DecoratorBuilder {

	private List<InterceptorManager> interceptors;
	private Map<Class<?>, InterceptorManager> interceptorsByType;

	public InterceptorDecorator(List<InterceptorManager> interceptors) {
		this.interceptors = interceptors;
		interceptorsByType = Func.index(interceptors, InterceptorManager::getInterceptorType);
	}

	@Override
	public AbstractProvider<Object> decorate(AbstractProvider<Object> provider) {

		Set<Annotation> bindings = new HashSet<>();
		List<InterceptorManager> all = getInterceptors(bindings, provider.getResolvedType().getAnnotations());

		Map<Method, List<InterceptorManager>> byMethod;
		Map<Method, Method> translate;

		if (provider.getType().isInterface()) {
			byMethod = new HashMap<>();
			translate = new HashMap<>();

			for (Method method : provider.getType().getMethods()) {
				Class<?>[] paramTypes = new Class[method.getParameterCount()];
				Type[] parameters = method.getGenericParameterTypes();
				Map<Type, Type> resolve = null;
				for (int i = 0; i < paramTypes.length; i++) {
					Type p = parameters[i];
					while (p instanceof TypeVariable) {
						if (resolve == null) {
							resolve = Reflect
									.getResolveTypes(method.getDeclaringClass(), provider.getResolvedGenericType())
									.getValue();
						}
						Type resolved = resolve.get(p);
						if (resolved == null) {
							break;
						} else {
							p = resolved;
						}
					}
					paramTypes[i] = Reflect.toClass(p);
				}
				Method m = null;
				try {
					m = provider.getResolvedType().getMethod(method.getName(), paramTypes);
				} catch (NoSuchMethodException ex2) {
					for (Method me : provider.getResolvedType().getMethods()) {
						if (method.getName().equals(me.getName())
								&& method.getParameterCount() == me.getParameterCount()) {
							boolean ok = true;
							Type[] meps = me.getGenericParameterTypes();
							Type[] methodps = method.getGenericParameterTypes();
							for (int i = 0; ok && i < meps.length; i++) {
								if (!meps[i].equals(methodps[i])) {
									Type t = methodps[i];
									ok = false;
									for (int j = 0; j < resolve.size() && t != null; j++) {
										t = resolve.get(t);
										if (!meps[i].equals(t)) {
											ok = true;
											break;
										}
									}
								}
							}
							if(ok) {
								m = me;
								break;
							}
						}
					}
				}
				
				if(m == null) {
					continue;
				}
				translate.put(method, m);
				Set<Annotation> mbindings = new HashSet<>(bindings);
				List<InterceptorManager> list = getInterceptors(mbindings, m.getAnnotations());
				if (!m.isAnnotationPresent(ExcludeClassInterceptors.class)) {
					for (InterceptorManager im : all) {
						if (!list.contains(im)) {
							list.add(im);
						}
					}
				}
				if (!list.isEmpty()) {
					byMethod.put(m, list);
					Reflect.enable(m);
				}
			}
		} else {
			byMethod = Collections.emptyMap();
			translate = null;
		}

		if (!byMethod.isEmpty()) {
			Set<InterceptorManager> allInterceptors = new HashSet<>();
			for (List<InterceptorManager> im : byMethod.values()) {
				allInterceptors.addAll(im);
			}
			provider.setBuilder(
					new InterceptorInstanceCreator(provider.getBuilder(), allInterceptors, byMethod, translate));
		} else if (!all.isEmpty()) {
			provider.setBuilder(
					new InterceptorInstanceCreator(provider.getBuilder(), all, Collections.emptyMap(), translate));
		}
		return provider;
	}

	private List<InterceptorManager> getInterceptors(Set<Annotation> bindings, Annotation[] annotations) {
		List<InterceptorManager> all = new ArrayList<>();
		for (Annotation annotation : annotations) {
			if (annotation.annotationType().isAnnotationPresent(InterceptorBinding.class)) {
				bindings.add(annotation);
			} else if (annotation.annotationType() == Interceptors.class) {
				for (Class<?> cl : ((Interceptors) annotation).value()) {
					InterceptorManager im = interceptorsByType.get(cl);
					if (im != null) {
						all.add(im);
					}
				}
			}
		}
		if (!bindings.isEmpty()) {
			for (InterceptorManager im : interceptors) {
				if (im.getBindings().containsAll(bindings) && !all.contains(im)) {
					all.add(im);
				}
			}
		}
		return all;
	}

}
