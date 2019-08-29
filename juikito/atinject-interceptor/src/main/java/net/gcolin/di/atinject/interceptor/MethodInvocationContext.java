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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.interceptor.InvocationContext;

/**
 * InvocationContext for intercepting a method.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class MethodInvocationContext implements InvocationContext {

	private Object target;
	private Method method;
	private Object[] parameters;
	private Map<String, Object> context = new HashMap<>();

	public MethodInvocationContext(Object target, Method method, Object[] arguments) {
		this.target = target;
		this.method = method;
		parameters = arguments;
	}

	@Override
	public Object getTarget() {
		return target;
	}

	@Override
	public Object getTimer() {
		return null;
	}

	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public Constructor<?> getConstructor() {
		return null;
	}

	@Override
	public Object[] getParameters() {
		return parameters;
	}

	@Override
	public void setParameters(Object[] params) {
		this.parameters = params;
	}

	@Override
	public Map<String, Object> getContextData() {
		return context;
	}

	@Override
	public Object proceed() throws Exception {
		return CallUtil.call(method, target, getParameters());
	}

}
