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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Create injection point with the Inject annotation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InjectInjectionPointBuilder implements InjectionPointBuilder {

	@Override
	public InjectionPoint create(Field field, Environment env) {
		if (!Reflects.hasAnnotation(field.getAnnotations(), env.getInjectAnnotations())
				|| Modifier.isStatic(field.getModifiers())) {
			return null;
		}
		return new FieldInjectionPoint(field,
				env.getProvider(field.getType(), field.getGenericType(), env.findQualifiers(field.getAnnotations())));
	}

	@Override
	public InjectionPoint create(Method method, Environment env) {
		if (!Reflects.hasAnnotation(method.getAnnotations(), env.getInjectAnnotations())
				|| Modifier.isStatic(method.getModifiers())) {
			return null;
		}
		return new MethodInjectionPoint(method, InstanceBuilder.findProviders(method.getParameterTypes(),
				method.getGenericParameterTypes(), method.getParameterAnnotations(), env));
	}

}
