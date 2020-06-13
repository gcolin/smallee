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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.gcolin.common.collection.Collections2;
import net.gcolin.common.lang.Strings;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.core.AbstractEnvironment;
import net.gcolin.di.core.InjectException;
import net.gcolin.di.core.InjectService;
import net.gcolin.di.core.Key;
import net.gcolin.di.core.NotFoundException;

/**
 * The Bean manager.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Environment extends AbstractEnvironment<Class<?>> implements InjectService {

	private ReentrantLock lock = new ReentrantLock();
	protected static final String CANNOT_INJECT_2 = "cannot inject {0} because there are two possibilities : {1}, {2}";
	protected static final String CANNOT_INJECT_NO = "cannot inject {0} because there are no possibility";
	public static final Key NULL_PROVIDER = new Key();
	private Logger log = LoggerFactory.getLogger("net.gcolin.di");

	private Set<Class<? extends Annotation>> injectAnnotations = new HashSet<>();
	private Map<Key, AbstractProvider<Object>> providersref = new ConcurrentHashMap<>();
	private Map<String, Key> providersByName = new HashMap<>();
	private List<Class<?>> possibleClass = new ArrayList<>();
	private Map<Class<? extends Annotation>, ProviderBuilder> scopes = new HashMap<>();
	private InjectionPointBuilder[] injectionPointBuilders = { new InjectInjectionPointBuilder() };
	private InstanceFactoryBuilder[] instanceFactoryBuilders = { new InjectInstanceFactoryBuilder() };
	private DecoratorBuilder[] decoratorBuilders = {};
	private ClassLoader cl;
	private Resolver[] resolvers = { new ProviderResolver(this), new OptionalResolver(this),
			new OptionalIntResolver(this), new OptionalDoubleResolver(this), new OptionalLongResolver(this) };
	private boolean sealed = true;
	private List<Extension> extensions = Collections.emptyList();

	public Environment() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public Environment(ClassLoader cl) {
		injectAnnotations.add(Inject.class);
		injectAnnotations.add(net.gcolin.di.atinject.Inject.class);
		NULL_PROVIDER.setType("null");
		this.cl = cl;
		addProviderBuilder(new SingletonProviderBuilder());
		put(this, Environment.class);
	}

	public Logger getLog() {
		return log;
	}

	public void setLog(Logger log) {
		this.log = log;
	}

	protected List<Class<?>> getPossibleClass() {
		return possibleClass;
	}

	public void setSealed(boolean sealed) {
		this.sealed = sealed;
	}

	public Set<Class<? extends Annotation>> getInjectAnnotations() {
		return injectAnnotations;
	}

	public void addResolver(Resolver resolver) {
		resolvers = Collections2.addToArray(resolvers, resolver);
	}

	public void addDecoratorBuilders(DecoratorBuilder builder) {
		decoratorBuilders = Collections2.addToArray(decoratorBuilders, builder);
	}

	public void addInjectionPointBuilder(InjectionPointBuilder builder) {
		injectionPointBuilders = Collections2.addToArray(injectionPointBuilders, builder);
	}

	public void addInstanceFactoryBuilder(InstanceFactoryBuilder builder) {
		instanceFactoryBuilders = Collections2.addToArray(instanceFactoryBuilders, builder);
	}

	public InjectionPointBuilder[] getInjectionPointBuilders() {
		return injectionPointBuilders;
	}

	public InstanceFactoryBuilder[] getInstanceFactoryBuilders() {
		return instanceFactoryBuilders;
	}

	public ClassLoader getClassLoader() {
		return cl;
	}

	public void addProviderBuilder(ProviderBuilder builder) {
		scopes.put(builder.getScope(), builder);
	}

	protected List<Extension> loadExtensions() {
		return Collections2.toList(ServiceLoader.load(Extension.class, cl).iterator());
	}

	public void start() {
		extensions = loadExtensions();
		Collections.sort(extensions, new HasPriorityComparator());
		for (Extension extension : extensions) {
			log.info("start extension " + extension.getClass().getName());
			extension.doStart(this);
		}
		Collections.sort(possibleClass, new PriorityComparator());
		for (Extension extension : extensions) {
			extension.doStarted(this);
		}
		for (Class<?> possible : possibleClass) {
			if (possible.isAnnotationPresent(Startup.class)) {
				find(possible);
			}
		}
	}

	public List<Extension> getExtensions() {
		return extensions;
	}

	@SuppressWarnings("unchecked")
	public <T extends Extension> T getExtension(Class<T> type) {
		for (Extension extension : extensions) {
			if (extension.getClass() == type) {
				return (T) extension;
			}
		}
		return null;
	}

	public Collection<Class<?>> getBeanClasses() {
		return Collections.unmodifiableCollection(possibleClass);
	}

	public void removeNullProviders() {
		Set<String> providersByNameToRemove = new HashSet<>();
		for (Entry<String, Key> e : providersByName.entrySet()) {
			if (NULL_PROVIDER.equals(e.getValue())) {
				providersByNameToRemove.add(e.getKey());
			}
		}
		for (String s : providersByNameToRemove) {
			providersByName.remove(s);
		}
	}

	public void remove(Class<?> c) {
		possibleClass.remove(c);
		Set<Key> providersrefToRemove = new HashSet<>();
		Set<String> providersByNameToRemove = new HashSet<>();
		for (Entry<Key, AbstractProvider<Object>> o : providersref.entrySet()) {
			if (shouldBeRemoved(c, o.getValue())) {
				markRemoved(providersrefToRemove, providersByNameToRemove, o);
			}
		}
		for (Key s : providersrefToRemove) {
			providersref.remove(s);
		}
		for (String s : providersByNameToRemove) {
			providersByName.remove(s);
		}
	}

	public void remove(ClassLoader cl) {
		for (Class<?> c : possibleClass) {
			if (c.getClassLoader() == cl) {
				System.err.println(">>> memory leak " + c);
			}
		}
	}

	protected boolean shouldBeRemoved(Class<?> c, AbstractProvider<?> o) {
		return o.getType() == c;
	}

	private void markRemoved(Set<Key> providersrefToRemove, Set<String> providersByNameToRemove,
			Entry<Key, AbstractProvider<Object>> o) {
		providersrefToRemove.add(o.getKey());
		for (Entry<String, Key> e : providersByName.entrySet()) {
			if (e.getValue().equals(o.getKey())) {
				providersByNameToRemove.add(e.getKey());
			}
		}
	}

	public Binding addBinding(Class<?> c) {
		return new Binding(c, this);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) {
		return (T) find(clazz);
	}

	public Object find(Class<?> clazz) {
		return get(clazz, clazz, findQualifiers(clazz.getAnnotations()));
	}

	@SuppressWarnings("unchecked")
	public <T> T get(TypeLiteral<T> literal, Annotation... qualifiers) {
		return (T) get(literal.getRawType(), literal.getType(), qualifiers);
	}

	@SuppressWarnings("unchecked")
	public <T> Provider<T> getProvider(TypeLiteral<T> literal, Annotation... qualifiers) {
		return (Provider<T>) getProvider(literal.getRawType(), literal.getType(), qualifiers);
	}

	@SuppressWarnings("unchecked")
	public <T> AbstractProvider<T> getProvider(Class<T> clazz) {
		return (AbstractProvider<T>) getProvider(clazz, clazz, findQualifiers(clazz.getAnnotations()));
	}

	public void destroy(Instance instance) {
		instance.destroy(this);
	}

	@Override
	public Object find(String name) {
		AbstractProvider<?> provider = getProvider(name);
		if (provider == null) {
			return null;
		} else {
			return provider.get();
		}
	}

	public AbstractProvider<?> getProvider(String name) {
		Key s = providersByName.get(name);
		if (s == null) {
			lock.lock();
			try {
				s = providersByName.get(name);
				if (s == null) {
					for (Class<?> clazz : possibleClass) {
						Named named = clazz.getAnnotation(Named.class);
						if (named != null) {
							String val = named.value();
							if (val.isEmpty()) {
								val = Strings.uncapitalize(clazz.getSimpleName());
							}
							if (val.equals(name)) {
								s = createKey(clazz, clazz, findQualifiers(clazz.getAnnotations()));
								createProvider(s, clazz, clazz);
								break;
							}
						}
					}
					if (s == null) {
						s = NULL_PROVIDER;
						providersByName.put(name, NULL_PROVIDER);
					}
				}
			} finally {
				lock.unlock();
			}
		}
		return NULL_PROVIDER.equals(s) ? null : providersref.get(s);
	}

	public AbstractProvider<?> getProvider(Class<?> clazz, Type genericType, Annotation[] qualifiers) {
		return getProvider(createKey(clazz, genericType, qualifiers), clazz, genericType, qualifiers);
	}

	public void addProvider(Class<?> clazz, Type genericType, Annotation[] qualifiers,
			AbstractProvider<Object> provider) {
		Key key = createKey(clazz, genericType, qualifiers);
		addProvider(key, clazz, provider);
	}

	public void addProvider(Key key, Class<?> clazz, AbstractProvider<Object> provider) {
		if (key.getQualifiers() == null) {
			throw new InjectException("getQualifiers cannot be null");
		}

		provider.setKey(key);
		providersref.put(key, provider);
		Named named = provider.getResolvedType().getAnnotation(Named.class);
		if (named != null) {
			boolean ok = false;
			for (int i = 0; i < key.getQualifiers().length; i++) {
				if (key.getQualifiers()[i].annotationType() == Named.class) {
					ok = true;
					break;
				}
			}
			if (!ok) {
				Key k = new Key();
				k.setType(key.getType());
				Annotation[] oldqualifiers = key.getQualifiers();
				Annotation[] qualifiers = new Annotation[oldqualifiers.length + 1];
				System.arraycopy(oldqualifiers, 0, qualifiers, 0, oldqualifiers.length);
				qualifiers[oldqualifiers.length] = named;
				Arrays.sort(qualifiers, QUALIFIER_COMPARATOR);
				k.setQualifiers(qualifiers);
				key = k;
				providersref.put(key, provider);
			} else {
				Key k = new Key();
				k.setType(key.getType());
				Annotation[] oldqualifiers = key.getQualifiers();
				Annotation[] qualifiers = new Annotation[oldqualifiers.length - 1];
				for (int i = 0, j = 0; i < oldqualifiers.length; i++) {
					Annotation a = oldqualifiers[i];
					if (!a.equals(named)) {
						qualifiers[j++] = a;
					}
				}
				k.setQualifiers(qualifiers);
				provider.setKey(k);
				if (!providersref.containsKey(k)) {
					providersref.put(k, provider);
				}
			}

		}

		String name = getName(clazz, key.getQualifiers());
		if (name != null) {
			providersByName.put(name, key);
		}

	}

	public boolean isQualifier(Class<? extends Annotation> annotationType) {
		return annotationType.isAnnotationPresent(Qualifier.class);
	}

	public String getName(Class<?> clazz, Annotation[] qualifiers) {
		if (qualifiers != null) {
			for (int i = 0; i < qualifiers.length; i++) {
				Annotation a = qualifiers[i];
				if (a.annotationType() == Named.class) {
					return getName(clazz, (Named) a);
				}
			}
		}
		return null;
	}

	public String getName(Class<?> clazz, Named a) {
		String name = a.value();
		if (name.length() == 0) {
			name = clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);
		}
		return name;
	}

	protected AbstractProvider<?> getProvider(Key key, Class<?> clazz, Type genericType, Annotation[] qualifiers) {
		AbstractProvider<Object> p = providersref.get(key);
		if (p != null) {
			return p;
		}

		lock.lock();
		try {
			p = providersref.get(key);
			if (p != null) {
				return p;
			}
			return createProvider(key, clazz, genericType);
		} finally {
			lock.unlock();
		}
	}

	public AbstractProvider<Object> getProvider(Key key) {
		AbstractProvider<Object> p = providersref.get(key);
		if (p != null) {
			return p;
		}
		lock.lock();
		try {
			p = providersref.get(key);
			if (p != null) {
				return p;
			}

			try {
				Type type = Reflect.parseAsGeneric(key.getType(), cl, 0, key.getType().length());
				Class<?> clazz = Reflect.toClass(type);
				return createProvider(key, clazz, type);
			} catch (ClassNotFoundException ex) {
				throw new InjectException(ex);
			}

		} finally {
			lock.unlock();
		}
	}

	public Resolver[] getResolvers() {
		return resolvers;
	}

	protected AbstractProvider<Object> createProvider(Key key, Class<?> clazz, Type genericType) {
		Class<?> resolved = resolve(key, clazz, genericType);

		if (resolved == null) {
			for (int i = 0; i < resolvers.length; i++) {
				AbstractProvider<Object> provider = resolvers[i].find(clazz, genericType, key.getQualifiers());
				if (provider != null) {
					addProvider(key, clazz, provider);
					return provider;
				}
			}
			throw new NotFoundException(
					MessageFormat.format(CANNOT_INJECT_NO, createKey(clazz, genericType, key.getQualifiers())));
		}
		AbstractProvider<Object> p = providersref.get(key);
		if (p == null) {
			ProviderBuilder scope = getScope(resolved);
			p = (AbstractProvider<Object>) create0(clazz, genericType, resolved,
					resolved == clazz ? genericType : resolved, key.getQualifiers(),
					scope == null ? null : scope.getScope());
			if (resolved != clazz) {
				Key resolvedKey = createKey(resolved, resolved, key.getQualifiers());
				addProvider(resolvedKey, resolved, p);
			}
		}
		addProvider(key, clazz, p);
		return p;
	}

	@SuppressWarnings("unchecked")
	public <E, T extends E> void put(E o, Class<T> c) {
		Key key = createKey(c, c, findQualifiers(c.getAnnotations()));
		AbstractProvider<Object> provider = new SingletonProvider<Object>(o, (Class<Object>) c);
		InstanceCreator builder = new InstanceBuilder(this, true, provider);
		addProvider(key, c, provider);
		try {
			builder.bind(o, builder.getMetaData());
		} catch (Exception e) {
			throw new InjectException(e);
		}
	}

	@Override
	public void bind(Object o) {
		put(o, o.getClass());
	}

	public Environment addClasses(Class<?>... clazz) {
		for (int i = 0, l = clazz.length; i < l; i++) {
			add0(clazz[i]);
		}
		return this;
	}

	protected void add0(Class<?> c) {
		if (isInstanciable(c)) {
			add0NoCheck(c);
		}
	}

	protected void add0NoCheck(Class<?> c) {
		if (!possibleClass.contains(c)) {
			possibleClass.add(c);
		}
	}

	protected boolean isInstanciable(Class<?> c) {
		return !c.isInterface() && !Modifier.isAbstract(c.getModifiers());
	}

	protected Class<?> updateResolved(Class<?> clazz, Type genericType, Key key, Class<?> possible, Class<?> resolved) {
		if (resolved != null) {
			if (resolved == clazz || possible == clazz) {
				return clazz;
			}
			throw new InjectException(MessageFormat.format(CANNOT_INJECT_2, key, possible, resolved));
		}
		return possible;
	}

	public Class<?> resolve(Key key, Class<?> clazz, Type genericType) {
		Class<?> resolved = resolveBinding(key);
		if (resolved != null) {
			return resolved;
		}

		resolved = resolveFromPossibleClass(clazz, genericType, key);

		if (resolved == null && !sealed && clazz != null && isInstanciable(clazz)
				&& checkQualifiers(clazz, key.getQualifiers())) {
			resolved = clazz;
		}
		return resolved;
	}

	private Class<?> resolveFromPossibleClass(Class<?> clazz, Type genericType, Key key) {
		Class<?> resolved = null;
		if (genericType == clazz) {
			for (int i = 0, l = possibleClass.size(); i < l; i++) {
				Class<?> possible = possibleClass.get(i);
				if ((clazz == null || clazz.isAssignableFrom(possible))
						&& checkQualifiers(possible, key.getQualifiers())) {
					resolved = updateResolved(clazz, genericType, key, possible, resolved);
					break;
				}
			}
		} else {
			for (int i = 0, l = possibleClass.size(); i < l; i++) {
				Class<?> possible = possibleClass.get(i);
				if ((clazz == null || isAssignableGeneric(genericType, possible))
						&& checkQualifiers(possible, key.getQualifiers())) {
					resolved = updateResolved(clazz, genericType, key, possible, resolved);
					break;
				}
			}
			if (resolved == null) {
				return resolveFromPossibleClass(clazz, clazz, key);
			}
		}
		return resolved;
	}

	private boolean isAssignableGeneric(Type genericType, Class<?> possible) {
		Class<?> clazz = possible;
		Type generic = possible;
		while (clazz != Object.class && clazz != null) {
			if (generic instanceof ParameterizedType) {
				generic = Reflect.toType(possible, generic);
			}

			if (genericType.equals(generic) || isAssignableGenericInterface(clazz, genericType, possible)) {
				return true;
			}
			generic = clazz.getGenericSuperclass();
			clazz = clazz.getSuperclass();
		}
		return false;
	}

	private boolean isAssignableGenericInterface(Class<?> clazz, Type genericType, Class<?> possible) {
		if (clazz == null || clazz == Object.class) {
			return false;
		}
		for (Type intf : clazz.getGenericInterfaces()) {
			if (intf instanceof ParameterizedType) {
				intf = Reflect.toType(possible, intf);
			}
			if (genericType.equals(intf)
					|| isAssignableGenericInterface(Reflect.toClass(intf), genericType, possible)) {
				return true;
			}

		}
		return false;
	}

	protected boolean checkQualifiers(Class<?> c, Annotation[] qualifiers) {
		if (qualifiers == null || qualifiers.length == 0) {
			return true;
		}
		for (int i = 0, l = qualifiers.length; i < l; i++) {
			Annotation qualifer = qualifiers[i];
			Annotation a = c.getAnnotation(qualifer.annotationType());
			if ((a == null || !a.toString().equals(qualifer.toString())) && !checkNamed(c, a, qualifer)) {
				return false;
			}
		}
		return true;
	}

	protected boolean checkNamed(Class<?> c, Annotation a, Annotation qualifer) {
		if (a != null && a.annotationType() == Named.class) {
			return getName(c, (Named) a).equals(((Named) qualifer).value());
		}
		return false;
	}

	public Object get(Class<?> clazz, Type genericType, Annotation[] qualifiers) {
		if (clazz == Provider.class) {
			Type type = ((ParameterizedType) genericType).getActualTypeArguments()[0];
			return getProvider(Reflect.toClass(type), type, qualifiers);
		} else {
			return getProvider(clazz, genericType, qualifiers).get();
		}
	}

	public ProviderBuilder getScope(Class<?> clazz) {
		return getScope(clazz.getAnnotations());
	}

	public ProviderBuilder getScope(Annotation[] all) {
		for (int i = 0, l = all.length; i < l; i++) {
			Annotation a = all[i];
			ProviderBuilder builder = scopes.get(a.annotationType());
			if (builder != null) {
				return builder;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected AbstractProvider<Object> create0(final Class<?> clazz, Type genericType, final Class<?> resolvedClazz,
			Type resolvedGenericType, Annotation[] qualifiers, Class<? extends Annotation> scope) {
		AbstractProvider<Object> p = null;
		ProviderBuilder builder = scopes.get(scope);
		if (builder != null) {
			p = builder.create(clazz, genericType, resolvedClazz, resolvedGenericType, this);
		} else {
			p = new PrototypeProvider<Object>((Class<Object>) clazz, genericType, (Class<Object>) resolvedClazz,
					resolvedGenericType, this);
		}
		for (int i = 0; i < decoratorBuilders.length; i++) {
			p = decoratorBuilders[i].decorate(p);
		}

		return p;
	}

	public boolean isPossible(Class<?> c) {
		return possibleClass.contains(c);
	}

	public void add(Class<?>... classes) {
		addClasses(classes);
	}

	public void remove(Class<?>... classes) {
		for (Class<?> c : classes) {
			remove(c);
		}
		if (classes.length > 0) {
			// check classloader
			remove(classes[0].getClassLoader());
		}
	}

	public <E, T extends E> void bind(E o, Class<T> t) {
		put(o, t);
	}

	public void unbind(Object o) {
		Class<?> c = o.getClass();
		while (c != Object.class) {
			for (Field f : c.getDeclaredFields()) {
				if (f.isAnnotationPresent(Inject.class)) {
					try {
						f.setAccessible(true);
						f.set(o, null);
					} catch (IllegalAccessException e) {
						throw new InjectException(e);
					}
				}
			}
			for (Method f : c.getDeclaredMethods()) {
				if (f.isAnnotationPresent(Inject.class)) {
					try {
						Object[] args = new Object[f.getParameterCount()];
						for (int i = 0; i < args.length; i++) {
							args[i] = null;
						}
						try {
							f.invoke(o, args);
						} catch (InvocationTargetException e) {
							throw new InjectException(e);
						}
					} catch (IllegalAccessException e) {
						throw new InjectException(e);
					}
				}
			}
			c = c.getSuperclass();
		}
	}

	public <T> Supplier<T> findSupplier(Class<T> c) {
		final AbstractProvider<T> p = getProvider(c);
		return () -> {
			if (p == null) {
				return null;
			} else {
				return p.get();
			}
		};
	}

	@Override
	public String toString() {
		return "Juikito atinject " + possibleClass;
	}

	@Override
	public boolean isMutable(Class<?> type) {
		return false;
	}

	public void stop() {
		for (Extension extension : extensions) {
			extension.doStop(this);
		}
		for (AbstractProvider<?> provider : providersref.values()) {
			provider.stop();
		}
		for (Extension extension : extensions) {
			extension.doStopped(this);
		}
	}
}
