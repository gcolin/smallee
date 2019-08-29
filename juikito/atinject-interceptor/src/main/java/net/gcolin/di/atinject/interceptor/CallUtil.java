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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.gcolin.di.core.InjectException;

/**
 * Manage exception handling during method call.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class CallUtil {

	public static Object call(Method method, Object instance, Object... parameters) {
		try {
			return method.invoke(instance, parameters);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (InvocationTargetException ex) {
			if (ex.getCause() instanceof RuntimeException) {
				throw (RuntimeException) ex.getCause();
			} else {
				throw new InjectException(ex.getCause());
			}
		} catch (Throwable ex) {
			throw new InjectException(ex);
		}
	}

}
