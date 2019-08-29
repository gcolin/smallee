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

package net.gcolin.di.cdi.jpa.test.bean;

import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class Manager implements IManager {

  @Inject
  private IDao dao;
  
  /* (non-Javadoc)
   * @see net.gcolin.di.cdi.jpa.test.bean.IManager#find(java.lang.Long)
   */
  @Override
  @Transactional
  public Entity1 find(Long id) {
    return dao.find(id);
  }
  
  @Override
  @Transactional(Transactional.TxType.REQUIRES_NEW)
  public void callnever() {
    dao.never();
  }
  
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void callmandatory() {
    dao.mandatory();
  }
  
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void callnotsupported() {
    dao.notsupported();
  }

  @Override
  @Transactional
  public void callrequiredFailRuntimeException() {
    dao.requiredFailRuntimeException();
  }

  @Override
  @Transactional
  public void callrequiredFailException() throws Exception {
    dao.requiredFailException();
  }
  
}
