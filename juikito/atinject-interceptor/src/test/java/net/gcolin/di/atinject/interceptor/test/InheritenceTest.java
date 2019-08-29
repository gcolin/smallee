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
package net.gcolin.di.atinject.interceptor.test;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Singleton;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InterceptorBinding;
import javax.interceptor.InvocationContext;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.TypeLiteral;

/**
 * Test.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InheritenceTest {

	static boolean interceptorCalled;
	static boolean methodCalled;
	static boolean interceptorCalledEnd;

	interface Dao<T, K> {

		T find(K id);
		
		void save(T obj);
	}

	interface SuperDao<T> extends Dao<T, Long> {

		T findMany();
	}

	@Retention(RUNTIME)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	@InterceptorBinding
	public static @interface Bind {

	}

	public static class DaoImpl<T> implements Dao<T, Long> {

		@Bind
		@Override
		public T find(Long id) {
			methodCalled = true;
			return null;
		}
		
		@Bind
		@Override
		public void save(T obj) {
		}

	}

	@Singleton
	public static class SuperDaoImpl extends DaoImpl<String> implements SuperDao<String> {

		@Bind
		@Override
		public String findMany() {
			methodCalled = true;
			return null;
		}

	}

	@Bind
	@javax.interceptor.Interceptor
	public static class Interceptor {

		@AroundInvoke
		public void aroundInvoke(InvocationContext ctx) throws Exception {
			interceptorCalled = true;
			Assert.assertNotNull(ctx.getTarget());
			ctx.proceed();
			Assert.assertTrue(methodCalled);
			interceptorCalledEnd = true;
		}

	}

	@SuppressWarnings("serial")
	@Test
	public void test() {
		Environment env = new Environment();
		env.add(SuperDaoImpl.class, Interceptor.class);
		env.start();

		interceptorCalled = false;
		methodCalled = false;
		interceptorCalledEnd = false;

		SuperDao<String> dao = env.get(new TypeLiteral<SuperDao<String>>() {
		});

		dao.find(1L);

		Assert.assertTrue(interceptorCalled);
		Assert.assertTrue(methodCalled);
		Assert.assertTrue(interceptorCalledEnd);
	}

}
