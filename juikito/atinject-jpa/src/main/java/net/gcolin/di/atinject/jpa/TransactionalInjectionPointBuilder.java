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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.FieldInjectionPoint;
import net.gcolin.di.atinject.InjectionPoint;
import net.gcolin.di.atinject.InjectionPointBuilder;

/**
 * Create inject points for PersistenceContext.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class TransactionalInjectionPointBuilder implements InjectionPointBuilder {

    private JpaExtension extension;

    public TransactionalInjectionPointBuilder(JpaExtension extension) {
        this.extension = extension;
    }
    
    @Override
    public InjectionPoint create(Field field, Environment env) {
        PersistenceContext pc = field.getAnnotation(PersistenceContext.class);
        if (pc != null) {
            return new FieldInjectionPoint(field, new EntityManagerSupplier(pc, extension, field.getDeclaringClass().getClassLoader()));
        }
        PersistenceUnit pu = field.getAnnotation(PersistenceUnit.class);
        if (pu != null) {
            return new FieldInjectionPoint(field, new EntityManagerFactorySupplier(pu, extension, field.getDeclaringClass().getClassLoader()));
        }
        return null;
    }

    @Override
    public InjectionPoint create(Method method, Environment env) {
        return null;
    }

}
