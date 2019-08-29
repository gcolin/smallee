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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;

/**
 * Provide an EntityManager Proxy.
 *
 * @author Gaël COLIN
 * @since 1.0
 */
class EntityManagerSupplier extends EntityManagerFactoryFinder implements Supplier<EntityManager> {

    private EntityManager proxy;
    private final PersistenceContext pc;

    public EntityManagerSupplier(PersistenceContext pc, JpaExtension extension,
            ClassLoader classLoader) {
        super(extension, classLoader);
        this.pc = pc;
    }

    @Override
    public EntityManager get() {
        if (proxy == null) {
            proxy = (EntityManager) Proxy.newProxyInstance(EntityManager.class.getClassLoader(),
                    new Class[]{EntityManager.class},
                    new java.lang.reflect.InvocationHandler() {

                EntityManagerFactory emf = getEmf();

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return method.invoke(PersistenceContexts.getEntityManager(emf), args);
                }
            });
        }
        return proxy;
    }

    @Override
    protected String key() {
        return pc.unitName();
    }

    @Override
    protected String unitName() {
        return pc.unitName();
    }

    @Override
    protected Map<String, Object> properties() {
        Map<String, Object> properties = new HashMap<>();
        for (PersistenceProperty prop : pc.properties()) {
            properties.put(prop.name(), prop.value());
        }
        return properties;
    }

}
