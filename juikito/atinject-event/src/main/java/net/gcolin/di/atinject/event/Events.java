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
package net.gcolin.di.atinject.event;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.inject.Singleton;

import net.gcolin.common.collection.ArrayQueue;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.atinject.AbstractProvider;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Resolver;
import net.gcolin.di.atinject.SupplierProvider;
import net.gcolin.di.atinject.jmx.JmxExtension;
import net.gcolin.di.core.InjectException;
import net.gcolin.di.core.Key;

/**
 * The events manager.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Events implements Event<Object>, Resolver {

	private List<Item> all = Collections.synchronizedList(new ArrayList<>());
	private Map<Key, EventDelegate> map = new HashMap<>();
	private Environment environment;
	private Map<String, Queue<Runnable>> asyncQueue = new HashMap<>();
	private static AtomicInteger index = new AtomicInteger(0);
	private ExecutorService executor;
	private JmxExtension jmx;

	public Events(Environment environment, JmxExtension jmx) {
		this.environment = environment;
		this.jmx = jmx;
	}

	public synchronized void observes(Method method, AbstractProvider<Object> provider, Async async) {
		Item item;
		if (async != null) {
			AsyncItem asyncItem = new AsyncItem();
			item = asyncItem;
			asyncItem.queue = asyncQueue.get(async.value());
			if (asyncItem.queue == null) {
				if (executor == null) {
					executor = Executors.newCachedThreadPool(new ThreadFactory() {

						@Override
						public Thread newThread(Runnable run) {
							Thread tr = new Thread(run);
							tr.setName("async-event-" + index.incrementAndGet());
							if (index.get() == Integer.MAX_VALUE) {
								index.set(0);
							}
							return tr;
						}
					});
				}
				asyncItem.queue = new AsyncQueue(async.value(), executor, new ArrayQueue<>(), async.size());
				jmx.add(asyncItem.queue);
				asyncQueue.put(async.value(), asyncItem.queue);
			} else if (asyncItem.queue.size() != async.size() && environment.getLog().isWarnEnabled()) {
				environment.getLog().warn("the queue " + async.value() + " is used many times with different sizes ("
						+ asyncItem.queue.size() + " and " + async.size() + ")");
			}
		} else {
			item = new Item();
		}
		item.key = environment.createKey(method.getParameterTypes()[0], method.getGenericParameterTypes()[0],
				environment.findQualifiers(method.getParameterAnnotations()[0]));
		item.method = method;
		Reflect.enable(method);
		item.provider = provider;
		all.add(item);
		for (EventDelegate evt : map.values()) {
			if (evt.key.equals(item.key)) {
				evt.items.add(item);
			}
		}
	}

	@Override
	public void fire(Object event) {
		for (int i = 0; i < all.size(); i++) {
			all.get(i).fire(event);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized AbstractProvider<Object> find(Class<?> clazz, Type genericType, Annotation[] qualifiers) {
		if (clazz == Event.class && genericType instanceof ParameterizedType) {
			Type generictype = ((ParameterizedType) genericType).getActualTypeArguments()[0];
			Key key = environment.createKey(Reflect.toClass(generictype), generictype, qualifiers);
			EventDelegate event = findDelegate(key);
			return new SupplierProvider<>((Class<Object>) clazz, new Supplier<Object>() {

				@Override
				public Object get() {
					return event;
				}

			}, Singleton.class, environment);
		}
		return null;
	}

	public EventDelegate findDelegate(Key key) {
		EventDelegate event = map.get(key);
		if (event == null) {
			event = new EventDelegate();
			event.key = key;
			map.put(key, event);
			for (int i = 0; i < all.size(); i++) {
				if (all.get(i).key.equals(key)) {
					event.items.add(all.get(i));
				}
			}
		}
		return event;
	}

	public static class EventDelegate implements Event<Object> {

		List<Item> items = new ArrayList<>();
		Key key;

		@Override
		public void fire(Object event) {
			for (int i = 0; i < items.size(); i++) {
				items.get(i).fire(event);
			}
		}

	}

	private static class Item {
		Key key;
		Method method;
		AbstractProvider<Object> provider;

		void fire(Object o) {
			try {
				method.invoke(provider.get(), o);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
				throw new InjectException(ex);
			}
		}
	}

	private static class AsyncItem extends Item {
		Queue<Runnable> queue;

		@Override
		void fire(Object o) {
			queue.offer(() -> {
				super.fire(o);
			});
		}
	}

	public void close() {
		for (Queue<Runnable> queue : asyncQueue.values()) {
			jmx.remove(queue);
		}
		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}
	}

}
