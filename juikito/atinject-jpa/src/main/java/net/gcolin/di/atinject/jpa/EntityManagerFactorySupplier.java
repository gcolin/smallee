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

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/**
 * Provide an EntityManagerFactory supplier.
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public class EntityManagerFactorySupplier extends EntityManagerFactoryFinder implements Supplier<EntityManagerFactory> {

    private EntityManagerFactory emf;
    private final PersistenceUnit pc;

    public EntityManagerFactorySupplier(PersistenceUnit pc, JpaExtension extension,
            ClassLoader classLoader) {
        super(extension, classLoader);
        this.pc = pc;
    }

    @Override
    public EntityManagerFactory get() {
        if (emf == null) {
            emf = getEmf();
        }
        return emf;
    }

    @Override
    protected String key() {
        return pc.toString();
    }

    @Override
    protected String unitName() {
        return pc.unitName();
    }

    @Override
    protected Map<String, Object> properties() {
        return Collections.emptyMap();
    }

}
