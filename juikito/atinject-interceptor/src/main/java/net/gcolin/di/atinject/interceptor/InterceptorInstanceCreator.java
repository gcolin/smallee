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
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.interceptor.InvocationContext;

import net.gcolin.di.atinject.ForwardInstanceCreator;
import net.gcolin.di.atinject.Instance;
import net.gcolin.di.atinject.InstanceBuilderMetaData;
import net.gcolin.di.atinject.InstanceCreator;
import net.gcolin.di.core.InjectException;

/**
 * InstanceCreator for an intercepted instance.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InterceptorInstanceCreator extends ForwardInstanceCreator {

	private InterceptorManager[] interceptors;
	private Map<Method, InterceptorManager[]> invokes;
	private Map<Method, Method> invokesTranslate;

	public InterceptorInstanceCreator(InstanceCreator delegate, Collection<InterceptorManager> interceptors,
			Map<Method, List<InterceptorManager>> invokes, Map<Method, Method> invokesTranslate) {
		super(delegate);
		this.interceptors = interceptors.toArray(new InterceptorManager[interceptors.size()]);
		this.invokesTranslate = invokesTranslate;
		if (!invokes.isEmpty()) {
			this.invokes = new HashMap<>();
			for (Entry<Method, List<InterceptorManager>> entry : invokes.entrySet()) {
				this.invokes.put(entry.getKey(),
						entry.getValue().toArray(new InterceptorManager[entry.getValue().size()]));
			}
		}
	}

	@Override
	public Instance newInstance() {
		InstanceBuilderMetaData medataData = getMetaData();
		Instance o = createInstance(medataData);
		bind(o.get(), medataData);
		return completeInstance(o);
	}

	@Override
	public Instance completeInstance(Instance o) {
		if (invokes != null) {
			return new ProxyInstance(Proxy.newProxyInstance(getProvider().getType().getClassLoader(),
					new Class[] { getProvider().getType() }, new InterceptorHandler(invokes, invokesTranslate, o)), o);
		}
		return o;
	}

	@Override
	public Instance createInstance(InstanceBuilderMetaData medataData) {
		InvocationContext ctx = null;

		Instance instance = new Instance(getProvider());

		for (int i = 0; i < interceptors.length; i++) {
			InterceptorManager interceptor = interceptors[i];
			Instance interceptorInstance = interceptor.getProvider().getBuilder().newInstance();
			instance.addDependent(interceptorInstance);
			if (interceptor.getAroundContruct() != null) {
				if (ctx == null) {
					ctx = new ConstructInvocationContext(medataData.getInstanceFactory(),
							getProvider().getResolvedType());
				}
				ctx = new InterceptorInvocationContext(ctx, interceptor.getAroundContruct(), interceptorInstance.get());
			}
		}

		if (ctx != null) {
			try {
				instance.setValue(ctx.proceed());
			} catch (Exception ex) {
				throw new InjectException(ex);
			}
		} else {
			Instance tmp = getDelegate().createInstance(medataData);
			instance.setValue(tmp.get());
			for (Instance dep : tmp.getDependents().values()) {
				instance.addDependent(dep);
			}
		}

		return instance;
	}

}
