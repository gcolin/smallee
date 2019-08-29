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

package net.gcolin.rest.param;

import net.gcolin.rest.server.ServerInvocationContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.ProcessingException;

/**
 * The BeanParam builds a bean parameter from the request.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see javax.ws.rs.BeanParam
 */
public class BeanParam extends Param {

	private Class<?> clazz;
	private Field[] fields;
	private Param[] params;

	/**
	 * Create a bean param.
	 * 
	 * @param clazz  the bean class
	 * @param fields the bean fields
	 * @param params the bean params. sizeof(params) = sizeof(fields)
	 */
	public BeanParam(Class<?> clazz, Field[] fields, Param[] params) {
		this.clazz = clazz;
		this.fields = fields;
		this.params = params;
	}

	@Override
	public Object update(ServerInvocationContext context) throws IOException {
		Object obj = null;
		try {
			obj = clazz.getDeclaredConstructor().newInstance();
			for (int i = 0, l = fields.length; i < l; i++) {
				Object val = params[i].update(context);
				if (val != null) {
					fields[i].set(obj, val);
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException ex) {
			throw new ProcessingException(ex);
		}
		return obj;
	}

}
