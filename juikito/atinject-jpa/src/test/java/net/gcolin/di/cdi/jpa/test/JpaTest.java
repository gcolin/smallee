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
package net.gcolin.di.cdi.jpa.test;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TransactionRequiredException;
import javax.transaction.TransactionalException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.cdi.jpa.test.bean.Dao;
import net.gcolin.di.cdi.jpa.test.bean.Entity1;
import net.gcolin.di.cdi.jpa.test.bean.IDao;
import net.gcolin.di.cdi.jpa.test.bean.IManager;
import net.gcolin.di.cdi.jpa.test.bean.Manager;

/**
 * Some tests with eclipselink and h2.
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class JpaTest {

	@Inject
	private IDao dao;

	@Inject
	private IManager manager;

	private Environment env;

	@Before
	public void before() {
		env = new Environment();
		env.add(Dao.class, Manager.class);
		env.start();

		env.bind(this);
	}

	@After
	public void after() {
		env.stop();
	}

	@Test
	public void simpleTest() {
		dao.never();

		Entity1 e1 = dao.add("hello");

		dao.never();

		Entity1 e2 = dao.find(e1.getId());
		Assert.assertEquals("hello", e2.getName());

		Assert.assertEquals("hello", manager.find(e1.getId()).getName());
		Assert.assertNotNull(dao.find("hello"));

		try {
			manager.callnever();
			Assert.fail();
		} catch (TransactionalException ex) {

		}

		manager.callmandatory();

		try {
			dao.mandatory();
			Assert.fail();
		} catch (TransactionRequiredException ex) {
			//
		}

		manager.callnotsupported();
		dao.notsupported();
	}

	@Test
	public void requiredExceptionTest() {
		try {
			dao.requiredFailRuntimeException();
			Assert.fail();
		} catch (RuntimeException ex) {
			Assert.assertEquals("runtime", ex.getMessage());
		}

		checkRollback();

		try {
			dao.requiredFailException();
			Assert.fail();
		} catch (Exception ex) {
			Assert.assertEquals(TransactionalException.class, ex.getClass());
			Assert.assertEquals("ex", ex.getCause().getMessage());
		}

		checkRollback();

		try {
			manager.callrequiredFailRuntimeException();
			Assert.fail();
		} catch (RuntimeException ex) {
			Assert.assertEquals("runtime", ex.getMessage());
		}

		checkRollback();

		try {
			manager.callrequiredFailException();
			Assert.fail();
		} catch (Exception ex) {
			Assert.assertEquals(TransactionalException.class, ex.getClass());
			Assert.assertEquals("ex", ex.getCause().getMessage());
		}

		checkRollback();
	}

	private void checkRollback() {
		try {
			dao.find("fail");
			Assert.fail();
		} catch (NoResultException ex) {
			//
		}
	}
}
