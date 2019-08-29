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

package net.gcolin.common.reflect;

import net.gcolin.common.Logs;
import net.gcolin.common.collection.ArrayQueue;
import net.gcolin.common.collection.Func;
import net.gcolin.common.lang.Pair;
import net.gcolin.common.lang.Strings;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Utility class for helping reflection
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Reflect {

	private static final String CANNOT_CREATE = "cannot create ";
	private static final String SET = "set";
	private static final String GET = "get";
	private static final String IS = "is";
	private static final Map<Class<?>, Class<?>> PRIMITIVE_EQUIVALENT = new HashMap<>();
	private static final Map<Class<?>, Class<?>> NON_PRIMITIVE_EQUIVALENT = new HashMap<>();
	public static final Function<Class<?>, Field[]> FIELD_ITERATOR = c -> c.getDeclaredFields();
	public static final Function<Class<?>, Method[]> METHOD_ITERATOR = c -> c.getDeclaredMethods();
	public static final Function<Class<?>, List<Field>> ALL_FIELDS = x -> find(x, FIELD_ITERATOR, e -> true,
			Reflect.flatList());

	static {
		PRIMITIVE_EQUIVALENT.put(int.class, Integer.class);
		PRIMITIVE_EQUIVALENT.put(long.class, Long.class);
		PRIMITIVE_EQUIVALENT.put(float.class, Float.class);
		PRIMITIVE_EQUIVALENT.put(double.class, Double.class);
		PRIMITIVE_EQUIVALENT.put(short.class, Short.class);
		PRIMITIVE_EQUIVALENT.put(boolean.class, Boolean.class);
		PRIMITIVE_EQUIVALENT.put(char.class, Character.class);
		PRIMITIVE_EQUIVALENT.put(byte.class, Byte.class);

		NON_PRIMITIVE_EQUIVALENT.put(Integer.class, int.class);
		NON_PRIMITIVE_EQUIVALENT.put(Long.class, long.class);
		NON_PRIMITIVE_EQUIVALENT.put(Float.class, float.class);
		NON_PRIMITIVE_EQUIVALENT.put(Double.class, double.class);
		NON_PRIMITIVE_EQUIVALENT.put(Short.class, short.class);
		NON_PRIMITIVE_EQUIVALENT.put(Boolean.class, boolean.class);
		NON_PRIMITIVE_EQUIVALENT.put(Character.class, char.class);
		NON_PRIMITIVE_EQUIVALENT.put(Byte.class, byte.class);
	}

	private Reflect() {
	}

	/**
	 * Check if an array of annotations has an annotation by name.
	 * 
	 * @param all  array of annotations
	 * @param type name of an annotation
	 * @return {@code true} if an annotation is found
	 */
	public static boolean hasAnnotation(Annotation[] all, String type) {
		for (int i = 0; i < all.length; i++) {
			if (all[i].annotationType().getName().equals(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if an array of annotations has an annotation by type.
	 * 
	 * @param <T>   the type of annotation
	 * @param array array of annotations
	 * @param clazz the type of annotation
	 * @return {@code true} if an annotation is found
	 */
	public static <T extends Annotation> boolean hasAnnotation(Annotation[] array, Class<T> clazz) {
		return getAnnotation(array, clazz) != null;
	}

	/**
	 * Convert a primitive to a non primitive.
	 * 
	 * <p>
	 * For example, {@code int} to {@code Integer}
	 * </p>
	 * 
	 * @param cl a type
	 * @return the input type or a non primitive equivalent
	 */
	public static Class<?> toNonPrimitiveEquivalent(Class<?> cl) {
		return cl.isPrimitive() ? PRIMITIVE_EQUIVALENT.get(cl) : cl;
	}

	/**
	 * Convert a non primitive to a primitive if possible.
	 * 
	 * <p>
	 * For example, {@code Integer} to {@code int}
	 * </p>
	 * 
	 * @param cl a type
	 * @return the input type or a primitive equivalent
	 */
	public static Class<?> toPrimitiveEquivalent(Class<?> cl) {
		Class<?> res = NON_PRIMITIVE_EQUIVALENT.get(cl);
		return res == null ? cl : res;
	}

	/**
	 * Return the first element of the list of list argument.
	 * 
	 * @param <T> the type of list elements
	 * @return a function
	 */
	public static final <T> BiFunction<List<List<T>>, Integer, T> first() {
		return (list, size) -> {
			for (int i = 0; list != null && i < list.size(); i++) {
				List<T> ilist = list.get(i);
				if (!ilist.isEmpty()) {
					return ilist.get(0);
				}
			}
			return null;
		};
	}

	/**
	 * Return a matrix of the list of list.
	 * 
	 * @param <T>   the type of the matrix
	 * @param clazz the type of the matrix
	 * @return a matrix
	 */
	@SuppressWarnings("unchecked")
	public static final <T> BiFunction<List<List<T>>, Integer, T[][]> array(Class<T> clazz) {
		return (list, size) -> {
			T[][] array = (T[][]) Array.newInstance(clazz, list.size(), 0);
			for (int i = 0; i < list.size(); i++) {
				List<T> ilist = list.get(i);
				T[] iarray = (T[]) Array.newInstance(clazz, ilist.size());
				ilist.toArray(iarray);
				array[i] = iarray;
			}
			return array;
		};
	}

	/**
	 * Return an array of the list of list.
	 * 
	 * @param <T>   the type of the array
	 * @param clazz the type of the array
	 * @return an array
	 */
	public static final <T> BiFunction<List<List<T>>, Integer, T[]> flat(Class<T> clazz) {
		return (list, size) -> {
			@SuppressWarnings("unchecked")
			T[] array = (T[]) Array.newInstance(clazz, size);
			int offset = 0;
			for (int i = 0; i < list.size(); i++) {
				List<T> ilist = list.get(i);
				for (int j = 0; j < ilist.size(); j++) {
					array[offset + j] = ilist.get(j);
				}
				offset += ilist.size();
			}
			return array;
		};
	}

	/**
	 * Return a list of the list of list.
	 * 
	 * @param <T> the type of the values
	 * @return a list
	 */
	public static final <T> BiFunction<List<List<T>>, Integer, List<T>> flatList() {
		return (list, size) -> {
			List<T> out = new ArrayList<>();
			for (int i = 0, l = list.size(); i < l; i++) {
				List<T> ilist = list.get(i);
				for (int j = 0, k = ilist.size(); j < k; j++) {
					out.add(ilist.get(j));
				}
			}
			return out;
		};
	}

	/**
	 * A generic function for finding objects from a class and convert it.
	 * 
	 * @param <T>       the type of the values
	 * @param <R>       the type of the result
	 * @param clazz     a class to analyze
	 * @param iterable  a function for extracting parts of the object such as
	 *                  fields, methods, constructors, ...
	 * @param accept    a filter
	 * @param converter a converter
	 * @return a result
	 */
	public static <T, R> R find(Class<?> clazz, Function<Class<?>, T[]> iterable, Predicate<T> accept,
			BiFunction<List<List<T>>, Integer, R> converter) {
		Class<?> cl = clazz;
		List<List<T>> list = new ArrayList<>();
		int size = 0;
		while (cl != Object.class && cl != null) {
			List<T> sl = new ArrayList<>();
			list.add(sl);
			T[] ar = iterable.apply(cl);
			for (int i = 0, m = ar.length; i < m; i++) {
				T el = ar[i];
				if (accept.test(el)) {
					sl.add(el);
					size++;
				}
			}
			cl = cl.getSuperclass();
		}
		if (size == 0) {
			return null;
		}
		return converter.apply(list, size);
	}

	/**
	 * A generic function for finding objects from a class and convert it.
	 * 
	 * @param <T>       the type of the values
	 * @param <R>       the type of the result
	 * @param clazz     a class to analyze
	 * @param iterable  a function for extracting parts of the object such as
	 *                  fields, methods, constructors, ...
	 * @param accept    a filter
	 * @param converter a converter
	 * @param hash      a hash function for filtering the results a second time
	 * @param added     a function for knowing if the element must be unique
	 * @return a result
	 */
	public static <T, R> R find(Class<?> clazz, Function<Class<?>, T[]> iterable, Predicate<T> accept,
			BiFunction<List<List<T>>, Integer, R> converter, Function<T, String> hash, Predicate<T> added) {
		Class<?> cl = clazz;
		List<List<T>> list = new ArrayList<>();
		int size = 0;
		Set<String> upper = new HashSet<>();
		Set<String> allmethods = new HashSet<>();
		while (cl != Object.class) {
			final List<T> l = new ArrayList<>();
			list.add(l);
			T[] ar = iterable.apply(cl);
			for (int i = 0, m = ar.length; i < m; i++) {
				T el = ar[i];
				String hs = hash.apply(el);
				if (!upper.contains(hs)) {
					boolean add = added.test(el);
					if (add) {
						upper.add(hs);
					}
					if (accept.test(el) && !allmethods.contains(hs)) {
						l.add(el);
						size++;
					}
					if (!add) {
						allmethods.add(hs);
					}
				}
			}
			cl = cl.getSuperclass();
		}
		if (size == 0) {
			return null;
		}
		return converter.apply(list, size);
	}

	/**
	 * A generic function for finding objects from an object and convert it.
	 * 
	 * @param <A>       the object type
	 * @param <T>       the type of the values
	 * @param <R>       the type of the result
	 * @param src       an object to analyze
	 * @param iterable  a function for extracting parts of the object
	 * @param accept    a filter
	 * @param converter a converter
	 * @return a result
	 */
	public static <A, T, R> R find(A src, Function<A, T[]> iterable, Predicate<T> accept,
			Function<List<T>, R> converter) {
		List<T> list = new ArrayList<>();
		T[] ar = iterable.apply(src);
		for (int i = 0, m = ar.length; i < m; i++) {
			T el = ar[i];
			if (accept.test(el)) {
				list.add(el);
			}
		}
		if (list.isEmpty()) {
			return null;
		}
		return converter.apply(list);
	}

	/**
	 * Create a new instance of a class and catch the exceptions.
	 * 
	 * @param className the class name to instance
	 * @return an instance or {@code null}
	 */
	public static Object newInstance(String className) {
		try {
			return Class.forName(className).getDeclaredConstructor().newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException ex) {
			String msg = CANNOT_CREATE + className;
			Logs.LOG.severe(msg);
			Logs.LOG.log(Level.FINE, msg, ex);
			return null;
		}
	}

	/**
	 * Create a new instance of a class and catch the exceptions.
	 * 
	 * @param <T>   the type of the instance
	 * @param clazz the class to instance
	 * @return an instance or {@code null}
	 */
	public static <T> T newInstance(Class<T> clazz) {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (IllegalStateException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException ex) {
			String msg = CANNOT_CREATE + clazz;
			Logs.LOG.severe(msg);
			Logs.LOG.log(Level.FINE, msg, ex);
			return null;
		}
	}

	/**
	 * Convert a generic type to a class.
	 * 
	 * @param type a generic type
	 * @return a class or {@code null}
	 */
	public static Class<?> toClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return toClass(((ParameterizedType) type).getRawType());
		} else {
			return Object.class;
		}
	}

	/**
	 * Get an annotation form an array of annotation by type.
	 * 
	 * @param <T>   the type of the annotation
	 * @param array an array of annotations
	 * @param clazz the finding type
	 * @return an annotation or {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T getAnnotation(Annotation[] array, Class<T> clazz) {
		for (int i = 0, l = array.length; i < l; i++) {
			if (array[i].annotationType() == clazz) {
				return (T) array[i];
			}
		}
		return null;
	}

	/**
	 * Check if the method is a getter.
	 * 
	 * @param method a method
	 * @return {@code true} if the method is a getter
	 */
	public static boolean isGetter(Method method) {
		return method.getParameterCount() == 0 && (isStdGetter(method) || isBoolGetter(method));
	}

	/**
	 * Check if the method is a setter.
	 * 
	 * @param method a method
	 * @return {@code true} if the method is a setter
	 */
	public static boolean isSetter(Method method) {
		return method.getParameterCount() == 1 && method.getName().startsWith(SET)
				&& isMaj(SET.length(), method.getName());
	}

	/**
	 * Check if the method is a getter with the pattern getXXX().
	 * 
	 * @param method a method
	 * @return {@code true} if the method is a getter
	 */
	public static boolean isStdGetter(Method method) {
		return method.getName().startsWith(GET) && isMaj(GET.length(), method.getName());
	}

	/**
	 * Check if the method is a getter with the pattern isXXX().
	 * 
	 * @param method a method
	 * @return {@code true} if the method is a getter
	 */
	public static boolean isBoolGetter(Method method) {
		return method.getName().startsWith(IS) && method.getReturnType() == boolean.class
				&& isMaj(IS.length(), method.getName());
	}

	private static boolean isMaj(int nb, String str) {
		return str.length() > nb && Character.isUpperCase(str.charAt(nb));
	}

	/**
	 * Get the property name of a getter.
	 * 
	 * <p>
	 * WARNING, before calling this method, you must be sure that the given method
	 * is a getter.
	 * </p>
	 * 
	 * <p>
	 * For example, isName or getName returns name
	 * </p>
	 * 
	 * @param getter a getter method
	 * @return the property name
	 */
	public static String getPropertyName(Method getter) {
		String name = getter.getName();
		if (name.startsWith(IS)) {
			return Character.toLowerCase(name.charAt(IS.length())) + name.substring(IS.length() + 1);
		} else {
			return Character.toLowerCase(name.charAt(GET.length())) + name.substring(GET.length() + 1);
		}
	}

	/**
	 * Find a field by name.
	 * 
	 * @param bean the class to look into
	 * @param name the name of the field
	 * @return a {@code Field} or {@code null}
	 */
	public static Field getFieldByName(Class<?> bean, String name) {
		return find(bean, FIELD_ITERATOR, x -> x.getName().equals(name), Reflect.first());
	}

	/**
	 * Find a getter associated a property name.
	 * 
	 * @param name   the property name
	 * @param parent the class to look into
	 * @return a {@code Method} or {@code null}
	 */
	public static Method findGetter(String name, Class<?> parent) {
		String n1 = getGetterStdName(name);
		String n2 = getGetterBoolName(name);
		return find(parent, Reflect.METHOD_ITERATOR, x -> x.getName().equals(n1) || x.getName().equals(n2),
				Reflect.first());
	}

	/**
	 * Find a getter associated a property name.
	 * 
	 * @param clazz  the class to look into
	 * @param name   the property name
	 * @param target the return type of the getter
	 * @return a {@code Method} or {@code null}
	 */
	public static Method getGetter(Class<?> clazz, String name, Class<?> target) {
		String methodName = target == boolean.class ? getGetterBoolName(name) : getGetterStdName(name);
		try {
			Method me = clazz.getMethod(methodName);
			if (me != null && me.getReturnType() == target) {
				return me;
			} else {
				return null;
			}
		} catch (NoSuchMethodException | SecurityException ex) {
			Logs.LOG.log(Level.FINE, ex.getMessage(), ex);
			return null;
		}
	}

	/**
	 * Find a setter associated a property name.
	 * 
	 * @param clazz  the class to look into
	 * @param name   the property name
	 * @param target the type of the setter argument
	 * @return a {@code Method} or {@code null}
	 */
	public static Method getSetter(Class<?> clazz, String name, Class<?> target) {
		String methodName = getSetterName(name);
		try {
			return clazz.getMethod(methodName, target);
		} catch (NoSuchMethodException | SecurityException ex) {
			Logs.LOG.log(Level.FINE, ex.getMessage(), ex);
			return null;
		}
	}

	public static String getSetterName(String name) {
		return SET + Strings.capitalize(name);
	}

	public static String getGetterStdName(String name) {
		return GET + Strings.capitalize(name);
	}

	public static String getGetterBoolName(String name) {
		return IS + Strings.capitalize(name);
	}

	public static Method findMethod(Class<?> clazz, String name, int paramCount) {
		return Reflect.find(clazz, Reflect.METHOD_ITERATOR,
				x -> x.getName().equals(name) && x.getParameterCount() == paramCount, Reflect.first());
	}

	public static List<Method> findMethods(Class<?> clazz, String name, int paramCount) {
		return Reflect.find(clazz, Reflect.METHOD_ITERATOR,
				x -> x.getName().equals(name) && x.getParameterCount() == paramCount, Reflect.flatList());
	}

	/**
	 * Get the type of a member.
	 * 
	 * @param member a member
	 * @return the field type, the return type or the declaring class.
	 */
	public static Class<?> getType(Member member) {
		if (member instanceof Field) {
			return ((Field) member).getType();
		}
		if (member instanceof Method) {
			return ((Method) member).getReturnType();
		}
		if (member instanceof Constructor) {
			return ((Constructor<?>) member).getDeclaringClass();
		}
		return null;
	}

	/**
	 * Get a class of a parameter.
	 * 
	 * @param member a constructor or a method
	 * @param index  an index
	 * @return a class or {@code null}
	 */
	public static Class<?> getType(Member member, int index) {
		if (member instanceof Executable) {
			return ((Executable) member).getParameterTypes()[index];
		}
		return null;
	}

	/**
	 * Resolve a TypeVariable.
	 * 
	 * @param baseClass    the base class
	 * @param childClass   the child class
	 * @param typeVariable a TypeVariable
	 * @return the typeVariable resolved
	 */
	public static Type getType(Class<?> baseClass, Type childClass, Type typeVariable) {
		Map<Type, Type> resolvedTypes = getResolveTypes(baseClass, childClass).getValue();
		Type baseType = typeVariable;
		while (resolvedTypes.containsKey(baseType)) {
			baseType = resolvedTypes.get(baseType);
		}
		return baseType;
	}

	/**
	 * Check if a class is defined in the ClassLoader.
	 * 
	 * @param clazz the class name
	 * @return {@code true} if the class is defined.
	 */
	public static boolean isDefined(String clazz) {
		try {
			Reflect.class.getClassLoader().loadClass(clazz);
			return true;
		} catch (ClassNotFoundException ex) {
			Logs.LOG.log(Level.FINE, ex.getMessage(), ex);
			return false;
		}
	}

	/**
	 * Enable a matrix of AccessibleObject.
	 * 
	 * @param array a matrix
	 */
	public static void enable(AccessibleObject[][] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, l = array.length; i < l; i++) {
			enable(array[i]);
		}
	}

	/**
	 * Enable an array of AccessibleObject.
	 * 
	 * @param array an array
	 */
	public static void enable(AccessibleObject[] array) {
		if (array == null) {
			return;
		}
		for (int i = 0, l = array.length; i < l; i++) {
			enable(array[i]);
		}
	}

	/**
	 * Enable an AccessibleObject.
	 * 
	 * @param elt an AccessibleObject
	 */
	public static void enable(AccessibleObject elt) {
		if (elt == null) {
			return;
		}
		elt.setAccessible(true);
	}

	/**
	 * Check if a method exists.
	 * 
	 * @param clazz      the class of the method
	 * @param methodName the name of the method
	 * @param returnType the return type of the method
	 * @param parameters the parameters of the method
	 * @return {@code true} if the method exists
	 */
	public static boolean exists(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameters) {
		try {
			return clazz.getMethod(methodName, parameters).getReturnType() == returnType;
		} catch (NoSuchMethodException | SecurityException ex) {
			Logs.LOG.log(Level.FINE, ex.getMessage(), ex);
			return false;
		}
	}

	/**
	 * Check if a class exists.
	 * 
	 * @param className the class name
	 * @param cl        ClassLoader
	 * @return {@code true} if the class exists
	 */
	public static boolean exists(String className, ClassLoader cl) {
		try {
			cl.loadClass(className);
			return true;
		} catch (ClassNotFoundException ex) {
			Logs.LOG.log(Level.FINE, ex.getMessage(), ex);
			return false;
		}
	}

	/**
	 * Check if a method exists.
	 * 
	 * @param clazz      the class of the method
	 * @param methodName the name of the method
	 * @param params     the parameters of the method
	 * @return {@code true} if the method exists
	 */
	public static boolean existsMethod(Class<?> clazz, String methodName, Class<?>... params) {
		try {
			clazz.getMethod(methodName, params);
			return true;
		} catch (NoSuchMethodException | SecurityException ex) {
			Logs.LOG.log(Level.FINE, ex.getMessage(), ex);
			return false;
		}
	}

	/**
	 * Get an enumeration value.
	 * 
	 * @param type  the className of the enumeration
	 * @param value the name of the enumeration item
	 * @return an enumeration value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object enumValue(final String type, String value) {
		try {
			return Enum.valueOf((Class<Enum>) Class.forName(type), value);
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Get an enumeration value.
	 * 
	 * @param <T>   the type of the enumeration
	 * @param type  the class of the enumeration
	 * @param value the name of the enumeration item
	 * @return an enumeration value
	 */
	public static <T extends Enum<T>> Enum<T> enumValue(final Class<T> type, String value) {
		return Enum.valueOf(type, value);
	}

	/**
	 * Get a method by name and argument number.
	 * 
	 * @param clazz    the class to look into
	 * @param name     the method name
	 * @param nbParams the parameter number
	 * @return a method or {@code null}
	 */
	public static Method getMethod(Class<?> clazz, String name, int nbParams) {
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals(name) && m.getParameterCount() == nbParams) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Execute a method.
	 * 
	 * @param obj    an instance.
	 * @param method the method name.
	 * @return The result of the method.
	 */
	public static Object execute(Object obj, String method) {
		try {
			return obj.getClass().getMethod(method).invoke(obj);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException ex) {
			Logs.LOG.log(Level.WARNING, ex.getMessage(), ex);
			return null;
		}
	}

	/**
	 * Execute a static method.
	 * 
	 * @param obj    an instance.
	 * @param method the method name.
	 * @return The result of the method.
	 */
	public static Object executeStatic(Class<?> obj, String method) {
		try {
			return obj.getMethod(method).invoke(null);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException ex) {
			Logs.LOG.log(Level.WARNING, ex.getMessage(), ex);
			return null;
		}
	}

	/**
	 * Execute a static method.
	 * 
	 * @param className the class name.
	 * @param method    the method name.
	 * @param cl        the class loader.
	 * @return The result of the method.
	 */
	public static Object executeStatic(String className, String method, ClassLoader cl) {
		try {
			return executeStatic(cl.loadClass(className), method);
		} catch (ClassNotFoundException ex) {
			Logs.LOG.log(Level.WARNING, ex.getMessage(), ex);
			return null;
		}
	}

	/**
	 * Get generic type of an implementation.
	 * 
	 * @param impl  the class to look into
	 * @param child the class to find the generic type
	 * @return a generic type
	 */
	public static Type getGenericType(Class<?> impl, Class<?> child) {
		Class<?> cl = impl;
		Type generic = impl;
		while (cl != null && cl != Object.class) {
			if (cl == child) {
				return generic;
			}
			Class<?>[] intf = cl.getInterfaces();
			for (int i = 0; i < intf.length; i++) {
				if (intf[i] == child) {
					return cl.getGenericInterfaces()[i];
				}
			}
			cl = cl.getSuperclass();
			generic = cl.getGenericSuperclass();
		}
		return child;
	}

	/**
	 * Get a java class in text.
	 * 
	 * @param type genericType
	 * @return a java class in text
	 */
	public static String toJavaClass(Type type) {
		if (type instanceof Class) {
			return ((Class<?>) type).getName().replace('$', '.');
		} else if (type instanceof ParameterizedType) {
			Class<?> clazz = Reflect.toClass(type);
			StringBuilder sb = new StringBuilder(clazz.getName().replace('$', '.'));
			sb.append('<');
			ParameterizedType pt = (ParameterizedType) type;
			sb.append(toJavaClass(pt.getActualTypeArguments()[0]));
			for (int i = 1; i < pt.getActualTypeArguments().length; i++) {
				sb.append(',');
				sb.append(toJavaClass(pt.getActualTypeArguments()[i]));
			}
			sb.append('>');
			return sb.toString();
		} else {
			return type.toString().replace('$', '.');
		}
	}

	/**
	 * Extract type variable if necessary from type.
	 * 
	 * @param type      for extracting type variable
	 * @param otherType base type
	 * @return a generic type
	 */
	public static Type toType(Type type, Type otherType) {
		if (otherType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) otherType;
			Type[] params = pt.getActualTypeArguments();
			Type[] resolvedParams = new Type[params.length];
			for (int i = 0; i < params.length; i++) {
				resolvedParams[i] = toType(type, params[i]);
			}
			return new ParametrizedP(Reflect.toClass(otherType), pt.getOwnerType(), resolvedParams);
		} else if (otherType instanceof TypeVariable) {
			TypeVariable<?> tv = (TypeVariable<?>) otherType;
			Type tp = type;
			while (tp != null && tp != Object.class) {
				Class<?> cl = Reflect.toClass(tp);
				if (tp instanceof ParameterizedType) {
					TypeVariable<?>[] vars = cl.getTypeParameters();
					for (int i = 0; i < vars.length; i++) {
						if (vars[i].equals(tv)) {
							return ((ParameterizedType) tp).getActualTypeArguments()[i];
						}
					}
				}
				Type[] interfaces = cl.getGenericInterfaces();
				for (int j = 0; j < interfaces.length; j++) {
					Type itf = interfaces[j];
					if (itf instanceof ParameterizedType) {
						TypeVariable<?>[] vars = Reflect.toClass(itf).getTypeParameters();
						for (int i = 0; i < vars.length; i++) {
							if (vars[i].equals(tv)) {
								return ((ParameterizedType) itf).getActualTypeArguments()[i];
							}
						}
					}
				}
				tp = cl.getGenericSuperclass();
			}
			return otherType;
		} else {
			return otherType;
		}
	}

	/**
	 * Parse a string to a genericType.
	 * 
	 * @param str string
	 * @return a genericType
	 * @throws ClassNotFoundException if an error occurs.
	 */
	public static Type parseAsGeneric(String str) throws ClassNotFoundException {
		return parseAsGeneric(str, Thread.currentThread().getContextClassLoader(), 0, str.length());
	}

	/**
	 * Parse a string to a genericType.
	 * 
	 * @param str   string
	 * @param cl    class loader
	 * @param start offset
	 * @param end   end
	 * @return a genericType
	 * @throws ClassNotFoundException if an error occurs.
	 */
	public static Type parseAsGeneric(String str, ClassLoader cl, int start, int end) throws ClassNotFoundException {
		if (str.charAt(end - 1) == '>') {
			int prec = str.indexOf('<', start);
			prec++;
			List<Type> params = new ArrayList<>();
			for (int i = prec; i < end - 1; i++) {
				if (str.charAt(i) == ',') {
					params.add(parseAsGeneric(str, cl, prec, i));
					prec = i + 1;
				}
			}
			params.add(parseAsGeneric(str, cl, prec, end - 1));
			Type ownerType;
			Class<?> rawType = cl.loadClass(str.substring(start, str.indexOf('<', start)));
			int ds = rawType.getName().lastIndexOf('$');
			if (ds != -1) {
				ownerType = cl.loadClass(rawType.getName().substring(0, ds));
			} else {
				ownerType = null;
			}
			return new ParametrizedP(rawType, ownerType, params.toArray(new Type[params.size()]));
		} else {
			return cl.loadClass(str.substring(start, end));
		}
	}

	private static class ParametrizedP implements ParameterizedType {

		private Type rawType;
		private Type ownerType;
		private Type[] arguments;

		public ParametrizedP(Type rawType, Type ownerType, Type[] arguments) {
			this.rawType = rawType;
			this.ownerType = ownerType;
			this.arguments = arguments;
		}

		@Override
		public Type getRawType() {
			return rawType;
		}

		@Override
		public Type getOwnerType() {
			return ownerType;
		}

		@Override
		public Type[] getActualTypeArguments() {
			return arguments;
		}

		@Override
		public String toString() {
			return rawType + "<" + Arrays.stream(arguments).map(x -> x.toString()).collect(Collectors.joining(", "))
					+ ">";
		}
	}

	/**
	 * Get the actual type arguments a child class has used to extend a generic base
	 * class.
	 * 
	 * @param baseClass  the base class
	 * @param childClass the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static Pair<Type, Map<Type, Type>> getResolveTypes(Class<?> baseClass, Type childClass) {
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type type = childClass;
		// start walking up the inheritance hierarchy until we hit baseClass
		Class<?> typeClass = Reflect.toClass(childClass);
		Set<Class<?>> intfDone = new HashSet<>();
		while (type != Object.class && type != null && !typeClass.equals(baseClass)) {
			boolean find = false;
			if (type instanceof ParameterizedType) {
				resolve0(resolvedTypes, type, typeClass);
			}

			if (!find) {
				Queue<Type> intfTodo = new ArrayQueue<>();
				addIntf(typeClass.getGenericInterfaces(), intfDone, intfTodo);
				while (!find && !intfTodo.isEmpty()) {
					Type gintf = intfTodo.poll();
					Class<?> intf = Reflect.toClass(gintf);
					if (gintf instanceof ParameterizedType) {
						resolve0(resolvedTypes, gintf, intf);
					}
					if (intf.equals(baseClass)) {
						type = gintf;
						typeClass = intf;
						find = true;
						break;
					} else {
						intfDone.add(intf);
						addIntf(intf.getGenericInterfaces(), intfDone, intfTodo);
					}
				}
			}

			// there is no useful information for us in raw types, so just
			// keep going.
			if (!find) {
				type = typeClass.getGenericSuperclass();
				typeClass = typeClass.getSuperclass();
			}
		}

		return new Pair<>(type, resolvedTypes);
	}

	private static void addIntf(Type[] intfs, Set<Class<?>> intfDone, Collection<Type> all) {
		for (int i = 0; i < intfs.length; i++) {
			if (!intfDone.contains(intfs[i])) {
				all.add(intfs[i]);
			}
		}
	}

	/**
	 * Resolve a generic class.
	 * 
	 * @param resolvedTypes a map of typeVariable to the type
	 * @param type          a ParameterizedType
	 * @param typeClass     a generic class.
	 */
	public static void resolve0(Map<Type, Type> resolvedTypes, Type type, Class<?> typeClass) {
		ParameterizedType parameterizedType = (ParameterizedType) type;
		Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
		TypeVariable<?>[] typeParameters = typeClass.getTypeParameters();
		for (int i = 0; i < actualTypeArguments.length; i++) {
			resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
		}
	}

	/**
	 * Resolve type variables.
	 * 
	 * @param baseClass  the base class
	 * @param childClass the child class
	 * @param holder     in the case of a field the associated type
	 * @return a list of resolved type variable class of the child class.
	 */
	public static List<Class<?>> getTypeArguments(Class<?> baseClass, Type childClass, Type holder) {
		return Func.map(getGenericTypeArguments(baseClass, childClass, holder), Reflect::toClass);
	}

	/**
	 * Resolve type variables as generic type.
	 * 
	 * @param <T>        the type of the baseClass
	 * @param baseClass  the base class
	 * @param childClass the child class
	 * @param holder     in the case of a field the associated type
	 * @return a list of resolved type variable generic type of the child class.
	 */
	@SuppressWarnings("rawtypes")
	public static <T> List<Type> getGenericTypeArguments(Class<T> baseClass, Type childClass, Type holder) {
		Pair<Type, Map<Type, Type>> resolvedTypes = getResolveTypes(baseClass, childClass);

		// finally, for each actual type argument provided to baseClass,
		// determine (if possible)
		// the raw class for that type argument.
		Type[] actualTypeArguments;
		Type type = resolvedTypes.getKey();
		if (type instanceof Class) {
			actualTypeArguments = ((Class) type).getTypeParameters();
		} else {
			actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
		}

		List<Type> typeArgumentsAsClasses = new ArrayList<>();
		// resolve types by chasing down type variables.
		for (int i = 0; i < actualTypeArguments.length; i++) {
			Type baseType = actualTypeArguments[i];
			while (resolvedTypes.getValue().containsKey(baseType)) {
				baseType = resolvedTypes.getValue().get(baseType);
			}
			if (baseType instanceof TypeVariable && holder != null) {
				baseType = getType(Object.class, holder, baseType);
			}
			if (baseType instanceof TypeVariable) {
				baseType = Object.class;
			}
			typeArgumentsAsClasses.add(baseType);
		}

		return typeArgumentsAsClasses;
	}
}
