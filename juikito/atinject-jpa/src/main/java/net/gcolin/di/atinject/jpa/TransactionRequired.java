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

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

/**
 * Interceptor for a Transactional REQUIRED.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Interceptor
@Transactional(value = TxType.REQUIRED)
public class TransactionRequired extends AbstractTransaction {

  /**
   * Intercept.
   * 
   * @param ctx a context
   * @return the response
   */
  @AroundInvoke
  public Object apply(InvocationContext ctx) {
    if (PersistenceContexts.push(Transactional.TxType.REQUIRED)) {
    	return proceed(ctx, Transactional.TxType.REQUIRED);
    } else {
    	return proceed(ctx);
    }
  }
}
