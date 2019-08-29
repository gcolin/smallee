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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;

/**
 * Add Transactional annotation to Atinject.
 *
 * @author Gaël COLIN
 * @since 1.0
 * @see Transactional
 */
public class JpaExtension implements Extension {

  private String defaultUnitPersistenceName;
  private Map<String, EntityManagerFactory> emfs = new ConcurrentHashMap<>();

  public Map<String, EntityManagerFactory> getEmfs() {
    return emfs;
  }

  @Override
  public void doStart(Environment env) {
    env.addClasses(TransactionMandatory.class, TransactionNever.class,
        TransactionNotSupported.class, TransactionRequired.class, TransactionRequiresNew.class);
    env.addInjectionPointBuilder(new TransactionalInjectionPointBuilder(this));
  }

  @Override
  public void doStop(Environment environment) {
    for (EntityManagerFactory emf : emfs.values()) {
      emf.close();
    }
  }

  public String getDefaultUnitPersistenceName() {
    return defaultUnitPersistenceName;
  }

  public void setDefaultUnitPersistenceName(String defaultUnitPersistenceName) {
    this.defaultUnitPersistenceName = defaultUnitPersistenceName;
  }

}
