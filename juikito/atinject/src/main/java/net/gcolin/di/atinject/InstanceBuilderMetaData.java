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

package net.gcolin.di.atinject;

import java.lang.reflect.Method;

/**
 * Data for creating an instance.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class InstanceBuilderMetaData {

    private InstanceFactory instanceFactory;
    private Method[][] postContructMethods;
    private Method[][] preDestroyMethods;
    private InjectionPoint[] injects;

    public Method[][] getPostContructMethods() {
        return postContructMethods;
    }

    public void setPostContructMethods(Method[][] postContructMethods) {
        this.postContructMethods = postContructMethods;
    }

    public Method[][] getPreDestroyMethods() {
        return preDestroyMethods;
    }

    public void setPreDestroyMethods(Method[][] preDestroyMethods) {
        this.preDestroyMethods = preDestroyMethods;
    }

    public InjectionPoint[] getInjects() {
      return injects;
    }

    public void setInjects(InjectionPoint[] injects) {
      this.injects = injects;
    }

    public InstanceFactory getInstanceFactory() {
      return instanceFactory;
    }

    public void setInstanceFactory(InstanceFactory instanceFactory) {
      this.instanceFactory = instanceFactory;
    }
}
