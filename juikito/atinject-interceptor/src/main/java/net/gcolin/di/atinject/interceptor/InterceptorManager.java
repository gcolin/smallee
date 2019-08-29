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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InterceptorBinding;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.PriorityFinder;

/**
 * An interceptor managed.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InterceptorManager {

	private Class<?> interceptorType;
	private Method aroundContruct;
	private Method aroundInvoke;
	private Set<Annotation> bindings;
	private AbstractProvider<Object> provider;
	private int priority = 0;

	public InterceptorManager(Class<?> interceptorType, AbstractProvider<Object> provider) {
		this.interceptorType = interceptorType;
		this.provider = provider;
		aroundContruct = Reflect.find(interceptorType, Reflect.METHOD_ITERATOR,
				m -> m.isAnnotationPresent(AroundConstruct.class), Reflect.first());
		aroundInvoke = Reflect.find(interceptorType, Reflect.METHOD_ITERATOR,
				m -> m.isAnnotationPresent(AroundInvoke.class), Reflect.first());
		if (aroundContruct != null) {
			Reflect.enable(aroundContruct);
		}
		if (aroundInvoke != null) {
			Reflect.enable(aroundInvoke);
		}

		for (Annotation annotation : interceptorType.getAnnotations()) {
			if (annotation.annotationType().isAnnotationPresent(InterceptorBinding.class)) {
				if (bindings == null) {
					bindings = new HashSet<>();
				}
				bindings.add(annotation);
			}
		}
		if (bindings == null) {
			bindings = Collections.emptySet();
		} else {
			bindings = Collections.unmodifiableSet(bindings);
		}
		priority = PriorityFinder.getPriority(interceptorType);
	}

	public Set<Annotation> getBindings() {
		return bindings;
	}

	public AbstractProvider<Object> getProvider() {
		return provider;
	}

	public int getPriority() {
		return priority;
	}

	public Class<?> getInterceptorType() {
		return interceptorType;
	}

	public Method getAroundContruct() {
		return aroundContruct;
	}

	public Method getAroundInvoke() {
		return aroundInvoke;
	}

	@Override
	public String toString() {
		return "InterceptorManager [bindings=" + bindings + "]";
	}

}
