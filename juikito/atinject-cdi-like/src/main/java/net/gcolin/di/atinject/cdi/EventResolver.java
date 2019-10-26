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

package net.gcolin.di.atinject.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Singleton;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Resolver;
import net.gcolin.di.atinject.SupplierProvider;
import net.gcolin.di.atinject.event.Events;

/**
 * A resolver for CDI Event.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class EventResolver implements Resolver, Event<Object> {

	private net.gcolin.di.atinject.event.Event<Object> eventDelegate;
	private Events events;
	private Environment env;
	private Annotation[] qualifiers;
	private Type type;

	public EventResolver(Events events, Environment env) {
		this(events, env, new Annotation[0], Object.class, events);
	}

	public EventResolver(Events events, Environment env, Annotation[] qualifiers, Type type,
			net.gcolin.di.atinject.event.Event<Object> eventDelegate) {
		this.events = events;
		this.env = env;
		this.eventDelegate = eventDelegate;
		this.type = type;
		this.qualifiers = qualifiers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public AbstractProvider<Object> find(Class<?> clazz, Type genericType, Annotation[] qualifiers) {
		if (clazz == Event.class && genericType instanceof ParameterizedType) {
			Type generictype = ((ParameterizedType) genericType).getActualTypeArguments()[0];
			net.gcolin.di.atinject.event.Event<Object> event = events
					.findDelegate(env.createKey(Reflect.toClass(generictype), generictype, qualifiers));

			return new SupplierProvider<>((Class<Object>) clazz, new Supplier<Object>() {

				@Override
				public Object get() {
					return event;
				}

			}, Singleton.class, env);
		}
		return null;
	}

	@Override
	public void fire(Object event) {
		eventDelegate.fire(event);
	}

	@Override
	public Event<Object> select(Annotation... qualifiers) {
		Set<Annotation> qualifierSet = new HashSet<>(Arrays.asList(this.qualifiers));
		qualifierSet.addAll(Arrays.asList(qualifiers));
		Annotation[] allQualifiers = qualifierSet.toArray(new Annotation[qualifierSet.size()]);

		net.gcolin.di.atinject.event.Event<Object> delegate = events
				.findDelegate(env.createKey(Reflect.toClass(type), type, allQualifiers));

		return new EventResolver(events, env, allQualifiers, type, delegate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
		Set<Annotation> qualifierSet = new HashSet<>(Arrays.asList(this.qualifiers));
		qualifierSet.addAll(Arrays.asList(qualifiers));
		Annotation[] allQualifiers = qualifierSet.toArray(new Annotation[qualifierSet.size()]);

		net.gcolin.di.atinject.event.Event<Object> delegate = events
				.findDelegate(env.createKey(subtype, subtype, allQualifiers));

		return (Event<U>) new EventResolver(events, env, allQualifiers, subtype, delegate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
		Set<Annotation> qualifierSet = new HashSet<>(Arrays.asList(this.qualifiers));
		qualifierSet.addAll(Arrays.asList(qualifiers));
		Annotation[] allQualifiers = qualifierSet.toArray(new Annotation[qualifierSet.size()]);

		net.gcolin.di.atinject.event.Event<Object> delegate = events
				.findDelegate(env.createKey(subtype.getRawType(), subtype.getType(), allQualifiers));

		return (Event<U>) new EventResolver(events, env, allQualifiers, subtype.getType(), delegate);
	}

	@Override
	public <U> CompletionStage<U> fireAsync(U event) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <U> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
		throw new UnsupportedOperationException();
	}

}
