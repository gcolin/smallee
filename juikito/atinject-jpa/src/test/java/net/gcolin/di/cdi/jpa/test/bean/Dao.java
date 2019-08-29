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

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
@Singleton
public class Dao implements IDao {

  @PersistenceContext
  private EntityManager em;

  /*
   * (non-Javadoc)
   * 
   * @see org.juikito.test.bean.IDAo#add(java.lang.String)
   */
  @Override
  @Transactional
  public Entity1 add(String name) {
    Entity1 entity = new Entity1();
    entity.setName(name);
    em.persist(entity);
    return entity;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.juikito.test.bean.IDAo#find(java.lang.Long)
   */
  @Override
  @Transactional
  public Entity1 find(Long id) {
    return em.find(Entity1.class, id);
  }

  @Override
  @Transactional
  public Entity1 find(String name) {
    return em.createQuery("select e from Entity1 e where e.name=:name", Entity1.class)
        .setParameter("name", name).getSingleResult();
  }

  @Transactional(Transactional.TxType.NEVER)
  public void never() {

  }

  @Transactional(Transactional.TxType.MANDATORY)
  public void mandatory() {

  }

  @Transactional(Transactional.TxType.NOT_SUPPORTED)
  public void notsupported() {

  }

  @Transactional
  public void requiredFailRuntimeException() {
    add("fail");
    throw new RuntimeException("runtime");
  }
  
  @Transactional
  public void requiredFailException() throws Exception {
    add("fail");
    throw new Exception("ex");
  }
}
