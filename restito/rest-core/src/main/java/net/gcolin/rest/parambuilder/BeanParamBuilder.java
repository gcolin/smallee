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

package net.gcolin.rest.parambuilder;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.param.BeanParam;
import net.gcolin.rest.param.Param;
import net.gcolin.rest.param.PostParam;
import net.gcolin.rest.server.ServerProviders;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * Build a BeanParam.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see javax.ws.rs.BeanParam
 *
 */
public class BeanParamBuilder implements ParamBuilder {

  private ServerProviders providers;

  public BeanParamBuilder(ServerProviders providers) {
    this.providers = providers;
  }

  @Override
  public Param build(Class<?> type, Type genericType, Annotation[] annotations, boolean multipart,
      Annotation annotation) {
    List<Field> fieldsList = new ArrayList<>();
    List<Param> paramsList = new ArrayList<>();
    Class<?> clazz = type;
    PostParam postParam = null;
    while (clazz != Object.class) {
      for (Field f : clazz.getDeclaredFields()) {
        if (Modifier.isTransient(f.getModifiers())) {
          continue;
        }
        Reflect.enable(f);
        Param param = providers.buildParam(f.getType(), f.getGenericType(), f.getAnnotations(),
            postParam == null, multipart);
        if (param instanceof PostParam) {
          postParam = (PostParam) param;
        }
        if (param != null) {
          paramsList.add(param);
          fieldsList.add(f);
        }
      }
      clazz = clazz.getSuperclass();
    }
    return new BeanParam(type, fieldsList.toArray(new Field[fieldsList.size()]),
        paramsList.toArray(new Param[paramsList.size()]));
  }

}
