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

package net.gcolin.di.atinject.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

import net.gcolin.di.atinject.Environment;

/**
 * A resolver for Expression language.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class EnvElResolver extends ELResolver {

  private static final String NOTFOUND = "notfound";
  private Environment env;

  public EnvElResolver(Environment env) {
    this.env = env;
  }

  @Override
  public Object getValue(final ELContext context, Object base, Object property) {
    if (base == null && property != null) {
      String propertyString = property.toString();
      EnvElContext cc = (EnvElContext) context.getContext(EnvElContext.class);
      if (cc == null) {
        cc = new EnvElContext();
        context.putContext(EnvElContext.class, cc);
      }
      Object obj = cc.get(propertyString);
      if (obj == null) {
        obj = env.find(propertyString);
        if (obj == null) {
          obj = NOTFOUND;
        }
        cc.put(propertyString, obj);
      }
      if (obj != NOTFOUND) {
        context.setPropertyResolved(true);
        return obj;
      }
    }
    return null;
  }

  @Override
  public Class<?> getType(ELContext context, Object base, Object property) {
    return null;
  }

  @Override
  public void setValue(ELContext context, Object base, Object property, Object value) {}

  @Override
  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return false;
  }

  @Override
  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
    return null;
  }

  @Override
  public Class<?> getCommonPropertyType(ELContext context, Object base) {
    return null;
  }

}
