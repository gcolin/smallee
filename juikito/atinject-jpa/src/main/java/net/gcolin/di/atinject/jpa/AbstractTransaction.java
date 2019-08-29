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
package net.gcolin.di.atinject.jpa;

import javax.interceptor.InvocationContext;
import javax.transaction.Transactional;
import javax.transaction.TransactionalException;

import net.gcolin.di.core.InjectException;

/**
 * AbstractTransaction.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class AbstractTransaction {

	public Object proceed(InvocationContext ctx) {
		try {
			return ctx.proceed();
		} catch (InjectException ex) {
			throw new TransactionalException(ex.getMessage(), ex.getCause());
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new TransactionalException(ex.getMessage(), ex);
		}
	}

	public Object proceed(InvocationContext ctx, Transactional.TxType type) {
		try {
			return ctx.proceed();
		} catch (InjectException ex) {
			PersistenceContexts.rollback(ex);
			throw new TransactionalException(ex.getMessage(), ex.getCause());
		} catch (RuntimeException ex) {
			PersistenceContexts.rollback(ex);
			throw ex;
		} catch (Exception ex) {
			PersistenceContexts.rollback(ex);
			throw new TransactionalException(ex.getMessage(), ex);
		} finally {
			PersistenceContexts.pop(type);
		}
	}

}
