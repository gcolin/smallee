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

package net.gcolin.rest.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.Environment;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.Logs;
import net.gcolin.rest.param.Param;
import net.gcolin.rest.router.Router;
import net.gcolin.rest.util.ReflectRest;

/**
 * Build REST services from an object or a class.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ResourceBuilder {
	
	private ServerProviders providers;
	private Router<ResourceArray> router;
	private Environment env;

	/**
	 * Create a ResourceBuilder.
	 * 
	 * @param providers the providers
	 * @param router    the router
	 * @param env       the bean provider
	 */
	public ResourceBuilder(ServerProviders providers, Router<ResourceArray> router, Environment env) {
		this.providers = providers;
		this.router = router;
		this.env = env;
	}

	/**
	 * Build resources from a POJO.
	 * 
	 * @param component      the singleton instance of the POJO
	 * @param componentClass the type of POJO
	 * @return the resources extracted.
	 */
	public List<AbstractResource> build(Object component, Class<?> componentClass) {
		Set<String> methodsWithResources = new HashSet<String>();
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		Class<?> clazz = componentClass;
		while (clazz != Object.class && clazz != null) {
			for (Method m : clazz.getDeclaredMethods()) {
				Set<String> httpMethods = getAllowedMethods(m);
				if (!httpMethods.isEmpty() && !methodsWithResources.contains(getMethodKey(m))) {
					resources.addAll(buildResources(m, router, env, component, methodsWithResources, componentClass,
							httpMethods));
				}
			}
			clazz = clazz.getSuperclass();
		}
		return resources;
	}

	/**
	 * Build resources.
	 * 
	 * @param component a builder
	 * @return resources generated.
	 */
	public List<AbstractResource> build(Builder component) {
		ResourceBuilderContext context = new ResourceBuilderContext();
		try {
			context.setClazz(Supplier.class);
			context.setEnv(env);

			Set<String> methodSet = new HashSet<>();
			methodSet.add("get");

			context.setMethodsWithResources(methodSet);
			context.setRouter(router);
			context.setConsumes(Arrays.stream(component.getConsumes()).map(x -> FastMediaType.valueOf(x))
					.collect(Collectors.toSet()));
			context.setProduces(Arrays.stream(component.getProduces()).map(x -> FastMediaType.valueOf(x))
					.collect(Collectors.toList()));
			context.setRootPath("");
			context.setMethodPath(component.getPath());
			context.setSingleton(component.getSupplier());
			if (context.getSingleton() == null) {
				context.setSingleton(component.getInstance());
				context.setMethod(component.getMethod());
			} else if (context.getSingleton() instanceof ResponseSupplier) {
				context.setMethod(ResponseSupplier.class.getMethod("get"));
			} else if (context.getSingleton() instanceof ResponseFunction) {
				context.setMethod(ResponseFunction.class.getMethod("apply"));
			}
			context.setCurrentMethod(context.getMethod());
			context.setCurrentClazz(Supplier.class);
			context.setHttpMethods(Arrays.stream(component.getMethods()).collect(Collectors.toSet()));
			buildResources(context);
		} catch (Exception ex) {
			Logs.LOG.error(ex.getMessage(), ex);
		}
		return context.getResources();
	}

	/**
	 * Build resources.
	 * 
	 * @param method               the method of the resource
	 * @param router               the router
	 * @param env                  the bean provider
	 * @param singleton            the POJO instance
	 * @param methodsWithResources names of method resource
	 * @param clazz                the POJO class
	 * @param httpMethods          the HTTP methods
	 * @return the resources extracted.
	 */
	public List<AbstractResource> buildResources(Method method, Router<ResourceArray> router, Environment env,
			Object singleton, Set<String> methodsWithResources, Class<?> clazz, Set<String> httpMethods) {
		ResourceBuilderContext context = new ResourceBuilderContext();
		try {
			context.setClazz(clazz);
			context.setEnv(env);
			context.setSingleton(singleton);
			context.setMethodsWithResources(methodsWithResources);
			context.setRouter(router);
			context.setConsumes(ReflectRest.buildMediaType(clazz.getAnnotation(Consumes.class),
					method.getDeclaringClass().getAnnotation(Consumes.class), method.getAnnotation(Consumes.class)));
			context.setProduces(ReflectRest.buildMediaType(clazz.getAnnotation(Produces.class),
					method.getDeclaringClass().getAnnotation(Produces.class), method.getAnnotation(Produces.class)));

			Path pathAnnotation = clazz.getAnnotation(Path.class);
			if (pathAnnotation != null) {
				context.setRootPath(pathAnnotation.value());
			} else {
				context.setRootPath("");
			}

			context.setMethodPath(createPath(context.getRootPath(), method.getAnnotation(Path.class)));
			context.setMethod(method);
			context.setCurrentMethod(method);
			context.setCurrentClazz(clazz);
			context.setHttpMethods(httpMethods);
			if (isNotPublicRestMethod(method)) {
				Logs.LOG.warn("the method {} MUST be public", method.getName());
			} else if (method.getDeclaringClass() != Object.class && !buildResources(context)) {
				Logs.LOG.debug("the method {} must have a http type", method.getName());
			}
		} catch (Exception ex) {
			Logs.LOG.error(ex.getMessage(), ex);
		}
		return context.getResources();
	}

	private boolean buildResources(ResourceBuilderContext context) {
		Method method = context.getCurrentMethod();
		if (!context.getHttpMethods().isEmpty()) {
			context.getMethodsWithResources().add(getMethodKey(method));

			Param[] params = null;
			if (method.getParameterTypes().length > 0) {
				params = buildParams(method, context.getClazz());
			}
			Resource resource = new Resource(context.getMethod(),
					context.getMethod().getReturnType() == Void.TYPE ? new ArrayList<FastMediaType>(0)
							: context.getProduces(),
					params, context.getConsumes(), context.getClazz(), providers);
			resource.setPath(context.getMethodPath());
			resource.setAllowedMethods(Collections.unmodifiableSet(context.getHttpMethods()));
			resource.setSingleton(context.getSingleton(), context.getEnv());
			context.getResources().add(resource);
			publishResource(context, getTypes(context.getHttpMethods()), resource);
			Logs.LOG.debug("register {}", context.getMethodPath());
			return true;
		} else if (isRestPossible(method)) {
			return buildResourceFromOverride(context);
		}
		return false;
	}

	public String getMethodKey(Method method) {
		return method.toString().replace(method.getDeclaringClass().getName(), "");
	}

	private boolean isRestPossible(Method method) {
		return method.getDeclaringClass() != Object.class && !method.getDeclaringClass().isInterface();
	}

	private void publishResource(ResourceBuilderContext context, List<Integer> types, Resource resource) {
		for (Integer type : types) {
			ResourceArray ra = new ResourceArray(resource.getPath());
			ra.add(type, resource);
			ra = context.getRouter().add(ra, 0, true);
			if (ra != null) {
				ResourceSelector rs = ra.get(type);
				if (rs != null) {
					publishResourceInCollection(resource, type, ra, rs);
				} else {
					ra.add(type, resource);
					if (Logs.LOG.isDebugEnabled()) {
						Logs.LOG.debug("resource " + resource.getPath() + " added with the http method "
								+ ResourceArray.toString(type));
					}
				}
			}
		}
	}

	private void publishResourceInCollection(Resource resource, Integer type, ResourceArray ra, ResourceSelector rs) {
		ResourceCollection collection;
		if (rs instanceof ResourceCollection) {
			collection = (ResourceCollection) rs;
		} else {
			collection = new ResourceCollection();
			ra.add(type, collection);
			collection.add((AbstractResource) rs);
		}
		collection.add(resource);
		if (Logs.LOG.isDebugEnabled()) {
			Logs.LOG.debug("resource " + resource.getPath() + " already added with the http method "
					+ ResourceArray.toString(type));
		}
	}

	private boolean isNotPublicRestMethod(Method method) {
		return !Modifier.isPublic(method.getModifiers()) && (method.isAnnotationPresent(Path.class)
				|| method.isAnnotationPresent(Produces.class) || method.isAnnotationPresent(Consumes.class));
	}

	private boolean buildResourceFromOverride(ResourceBuilderContext context) {
		// Super classes take precendence over interfaces
		Class<?> clazz = context.getCurrentClazz();
		Method method = context.getCurrentMethod();
		if (clazz.getSuperclass() != Object.class) {
			Class<?> sc = clazz.getSuperclass();
			Method me = getMethod(method.getName(), sc, method.getParameterTypes());
			if (me != null) {
				context.setCurrentClazz(sc);
				context.setCurrentMethod(me);
				boolean success = buildResources(context);
				if (success) {
					return true;
				}
			}
		}

		for (Class<?> ci : clazz.getInterfaces()) {
			Method me = getMethod(method.getName(), ci, method.getParameterTypes());
			if (me != null) {
				context.setCurrentClazz(ci);
				context.setCurrentMethod(me);
				boolean success = buildResources(context);
				if (success) {
					return true;
				}
			}
		}

		context.setCurrentClazz(clazz);
		context.setCurrentMethod(method);

		return false;
	}

	private Method getMethod(String name, Class<?> clazz, Class<?>[] parameterTypes) {
		try {
			return clazz.getMethod(name, parameterTypes);
		} catch (Exception ex) {
			if (Logs.LOG.isDebugEnabled()) {
				Logs.LOG.debug("cannot find " + name, ex);
			}
			return null;
		}
	}

	private Set<String> getAllowedMethods(Method method) {
		Set<String> types = new HashSet<>();
		for (Annotation annotation : method.getAnnotations()) {
			if (annotation.annotationType().isAnnotationPresent(HttpMethod.class)) {
				types.add(annotation.annotationType().getSimpleName());
			}
		}
		HttpMethod hm = method.getAnnotation(HttpMethod.class);
		if (hm != null) {
			types.add(hm.value());
		}
		return types;
	}

	private List<Integer> getTypes(Set<String> allowedMethods) {
		List<Integer> types = new ArrayList<>();
		for (String m : allowedMethods) {
			types.add(ResourceArray.toInt(m));
		}
		return types;
	}

	private Param[] buildParams(Method method, Class<?> componentClass) {
		boolean hasPostParam = false;
		Annotation[][] annotations = method.getParameterAnnotations();
		Class<?>[] types = method.getParameterTypes();
		Type[] genericTypes = method.getGenericParameterTypes();
		Param[] params = new Param[types.length];
		boolean multipart = isMultipart(method);
		Map<Type, Type> genericMap = null;
		for (int i = 0; i < annotations.length; i++) {
			Class<?> type = types[i];
			Type genericType = genericTypes[i];
			if (genericType instanceof TypeVariable) {
				if (genericMap == null) {
					genericMap = buildGenericMap(method, componentClass);
				}
				genericType = genericMap.get(genericType);
				type = Reflect.toClass(genericType);
			}
			params[i] = providers.buildParam(type, genericType, annotations[i], true, multipart);
			if (params[i] instanceof net.gcolin.rest.param.PostParam) {
				// data from post
				if (hasPostParam) {
					throw new ProcessingException("cannot have 2 post param : " + method);
				}
				hasPostParam = true;
			}
		}
		return params;

	}

	@SuppressWarnings({ "rawtypes" })
	private Map<Type, Type> buildGenericMap(Method method, Class<?> componentClass) {
		Map<Type, Type> genericMap;
		genericMap = new HashMap<>();
		List<Class<?>> list = Reflect.getTypeArguments(method.getDeclaringClass(), (Class) componentClass, null);
		TypeVariable<?>[] typesVar = method.getDeclaringClass().getTypeParameters();
		for (int j = 0; j < typesVar.length; j++) {
			genericMap.put(typesVar[j], list.get(j));
		}
		return genericMap;
	}

	private boolean isMultipart(Method method) {
		Consumes ca = method.getAnnotation(Consumes.class);
		if (ca == null) {
			return false;
		}
		for (String v : ca.value()) {
			if (v.startsWith("multipart/")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Create a path.
	 * 
	 * @param rootPath       the base path
	 * @param pathAnnotation the annotation path
	 * @return a text path
	 */
	public String createPath(String rootPath, Path pathAnnotation) {
		String path;
		if (pathAnnotation == null) {
			path = rootPath;
		} else {
			if (rootPath.length() > 0 && !rootPath.equals("/") && pathAnnotation.value().length() > 0) {
				if (pathAnnotation.value().startsWith("/")) {
					path = rootPath + pathAnnotation.value();
				} else {
					path = rootPath + "/" + pathAnnotation.value();
				}
			} else if (rootPath.length() > 0 && pathAnnotation.value().length() == 0) {
				path = rootPath;
			} else {
				path = pathAnnotation.value();
			}
		}
		if (path.equals("/")) {
			path = "";
		}
		return path;
	}
}
