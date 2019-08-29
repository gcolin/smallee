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

import net.gcolin.rest.param.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.FormParam;
import javax.ws.rs.ext.ParamConverterProvider;

/**
 * Build a Param from a FormParam annotation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see FormParam
 *
 */
public class FormParamBuilder implements ParamBuilder {

  private ParamConverterProvider paramProvider;

  public FormParamBuilder(ParamConverterProvider paramProvider) {
    this.paramProvider = paramProvider;
  }

  @Override
  public Param build(Class<?> type, Type genericType, Annotation[] annotations, boolean multipart,
      Annotation annotation) {
    FormParam form = (FormParam) annotation;
    if (multipart) {
      throw new IllegalStateException("you need servlet module for that");
    } else {
      net.gcolin.rest.param.QueryParam pp = new net.gcolin.rest.param.QueryParam(form.value());
      pp.setBoxer(paramProvider.getConverter(type, genericType, annotations));
      return pp;
    }
  }
}
