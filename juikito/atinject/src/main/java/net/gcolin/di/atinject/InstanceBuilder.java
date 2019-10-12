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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.logging.Level;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.core.InjectException;

/**
 * InstanceBuilder.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InstanceBuilder implements InstanceCreator {

	private Environment env;
	private boolean cached;
	private InstanceBuilderMetaData medata;
	private AbstractProvider<Object> provider;

	public InstanceBuilder(Environment env, boolean cached, AbstractProvider<Object> provider) {
		this.env = env;
		this.provider = provider;
		this.cached = cached;
		if (cached) {
			this.medata = buildMetaData();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gcolin.di.atinject.InstanceCreator#hasDestroyMethods()
	 */
	@Override
	public boolean hasDestroyMethods() {
		return this.medata != null && this.medata.getPreDestroyMethods() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gcolin.di.atinject.InstanceCreator#createDestroyHandler(java.lang.
	 * Object)
	 */
	@Override
	public Runnable createDestroyHandler(final Object o) {
		return () -> destroyInstance(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gcolin.di.atinject.InstanceCreator#destroyInstance(java.lang.Object)
	 */
	@Override
	public void destroyInstance(Object o) {
		Method[][] predestroy = null;
		if (this.medata != null) {
			predestroy = this.medata.getPreDestroyMethods();
		} else {
			predestroy = Reflects.findPredestroyMethods(provider.getResolvedType());
			Reflect.enable(predestroy);
		}
		if (predestroy != null) {
			preDestroy(o, predestroy);
		}
	}

	private void preDestroy(Object o, Method[][] predestroy) {
		try {
			for (int i = predestroy.length - 1; i > -1; i--) {
				Method[] m = predestroy[i];
				for (int j = 0; j < m.length; j++) {
					m[j].invoke(o);
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new InjectException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gcolin.di.atinject.InstanceCreator#newInstance()
	 */
	@Override
	public Instance newInstance() {
		InstanceBuilderMetaData medataData = getMetaData();
		Instance o = createInstance(medataData);
		bind(o.get(), medataData);
		return completeInstance(o);
	}

	@Override
	public Instance completeInstance(Instance o) {
		return o;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gcolin.di.atinject.InstanceCreator#createInstance(net.gcolin.di.
	 * atinject.InstanceBuilderMetaData)
	 */
	@Override
	public Instance createInstance(InstanceBuilderMetaData medataData) {
		if (medataData.getInstanceFactory() == null) {
			try {
				Object obj = provider.getResolvedType().getDeclaredConstructor().newInstance();
				return new Instance(obj, provider);
			} catch (Exception e) {
				throw new InjectException("cannot create " + provider.getResolvedType(), e);
			}
		} else {
			return medataData.getInstanceFactory().create();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gcolin.di.atinject.InstanceCreator#getMetaData()
	 */
	@Override
	public InstanceBuilderMetaData getMetaData() {
		InstanceBuilderMetaData medataData = this.medata;
		if (medataData == null) {
			medataData = buildMetaData();
		}
		if (cached) {
			this.medata = medataData;
		}
		return medataData;
	}

	private void postConstruct(Object o, InstanceBuilderMetaData medata) {
		try {
			for (int i = medata.getPostContructMethods().length - 1; i > -1; i--) {
				Method[] methods = medata.getPostContructMethods()[i];
				for (int j = 0; j < methods.length; j++) {
					methods[j].invoke(o);
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new InjectException("cannot postconstruct in " + provider.getResolvedType(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gcolin.di.atinject.InstanceCreator#bind(java.lang.Object)
	 */
	@Override
	public void bind(Object o) {
		bind(o, getMetaData());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.gcolin.di.atinject.InstanceCreator#bind(java.lang.Object,
	 * net.gcolin.di.atinject.InstanceBuilderMetaData)
	 */
	@Override
	public void bind(Object o, InstanceBuilderMetaData medata) {
		InjectionPoint[] points = medata.getInjects();
		if (points != null) {
			for (int i = 0; i < points.length; i++) {
				points[i].inject(o);
			}
		}

		if (medata.getPostContructMethods() != null) {
			postConstruct(o, medata);
		}
	}

	protected InstanceBuilderMetaData buildMetaData() {
		Class<?> clazz = provider.getResolvedType();
		env.getLog().log(Level.FINE, "create metadata of {0}", clazz);
		InstanceBuilderMetaData m = new InstanceBuilderMetaData();
		InstanceFactoryBuilder[] ifb = env.getInstanceFactoryBuilders();
		for (int i = 0; i < ifb.length && m.getInstanceFactory() == null; i++) {
			m.setInstanceFactory(ifb[i].create(provider, env));
		}
		m.setInjects(Reflects.findInjectPoints(clazz, env));
		if (!clazz.isInterface()) {
			m.setPostContructMethods(Reflects.findPostconstructMethods(clazz));
			Reflect.enable(m.getPostContructMethods());
			if (cached) {
				m.setPreDestroyMethods(Reflects.findPredestroyMethods(clazz));
				Reflect.enable(m.getPreDestroyMethods());
			}
		}
		return m;
	}

	public static AbstractProvider<?>[] findProviders(Class<?>[] types, Type[] genericType, Annotation[][] annotations,
			Environment env) {
		AbstractProvider<?>[] args = new AbstractProvider[types.length];
		for (int i = 0; i < types.length; i++) {
			args[i] = env.getProvider(types[i], genericType[i], env.findQualifiers(annotations[i]));
		}
		return args;
	}

	public static Object[] getArguments(AbstractProvider<?>[] providers) {
		Object[] args = new Object[providers.length];
		for (int i = 0; i < providers.length; i++) {
			args[i] = providers[i].get();
		}
		return args;
	}

	@Override
	public AbstractProvider<Object> getProvider() {
		return provider;
	}
}
