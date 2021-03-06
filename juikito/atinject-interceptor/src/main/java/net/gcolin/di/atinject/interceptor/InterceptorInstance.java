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

import java.lang.reflect.Method;

import javax.interceptor.InvocationContext;

/**
 * InvocationContext for an intercepted instance.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InterceptorInstance extends ForwardInvocationContext {

	private int priority;
	private Method method;
	private Object instance;

	public InterceptorInstance(InvocationContext delegate, Method method, Object instance, int priority) {
		super(delegate);
		this.method = method;
		this.instance = instance;
		this.priority = priority;
	}

	@Override
	public Object proceed() throws Exception {
		return CallUtil.call(method, instance, getDelegate());
	}

	public int getPriority() {
		return priority;
	}

}
