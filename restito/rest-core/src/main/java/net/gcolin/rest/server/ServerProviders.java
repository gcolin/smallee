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

package net.gcolin.rest.server;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.Environment;
import net.gcolin.rest.param.Param;
import net.gcolin.rest.param.ParsableParam;
import net.gcolin.rest.parambuilder.BeanParamBuilder;
import net.gcolin.rest.parambuilder.ContextParamBuilder;
import net.gcolin.rest.parambuilder.CookieParamBuilder;
import net.gcolin.rest.parambuilder.FormParamBuilder;
import net.gcolin.rest.parambuilder.HeaderParamBuilder;
import net.gcolin.rest.parambuilder.MatrixParamBuilder;
import net.gcolin.rest.parambuilder.ParamBuilder;
import net.gcolin.rest.parambuilder.PathParamBuilder;
import net.gcolin.rest.parambuilder.QueryParamBuilder;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.util.ParamConverterProviderImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

/**
 * The Providers implementation of the REST server side.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServerProviders extends SimpleProviders implements ParamConverterProvider {

  private List<ParamConverterProvider> list = new ArrayList<>();
  private final Map<Class<?>, ParamBuilder> paramBuilders = new HashMap<>();

  /**
   * Create a ServerProviders.
   */
  public ServerProviders() {
    super(RuntimeType.SERVER);
    paramBuilders.put(FormParam.class, new FormParamBuilder(this));
    paramBuilders.put(QueryParam.class, new QueryParamBuilder(this));
    paramBuilders.put(javax.ws.rs.HeaderParam.class, new HeaderParamBuilder(this));
    paramBuilders.put(javax.ws.rs.CookieParam.class, new CookieParamBuilder(this));
    paramBuilders.put(javax.ws.rs.PathParam.class, new PathParamBuilder(this));
    paramBuilders.put(javax.ws.rs.MatrixParam.class, new MatrixParamBuilder(this));
    paramBuilders.put(javax.ws.rs.BeanParam.class, new BeanParamBuilder(this));
    paramBuilders.put(Context.class, new ContextParamBuilder(this));
  }

  public Map<Class<?>, ParamBuilder> getParamBuilders() {
    return paramBuilders;
  }

  /**
   * Load the providers.
   * 
   * @param env a bean provider
   */
  public void load(Environment env) {
    super.load();
    list.clear();
    list.add(new ParamConverterProviderImpl());
    env.put(list.get(0));
  }

  public void add(ParamConverterProvider pconverter) {
    list.add(pconverter);
  }

  /**
   * Create a parameter.
   * 
   * @param type the parameter type
   * @param genericType the parameter generic type
   * @param annotations the annotations of the parameter
   * @param acceptPost the parameter can be a post parameter
   * @param multipart the parameter can be retrieved from a multipart request
   * @return a Param or {@code null} if it is not possible to create a Param
   */
  public Param buildParam(final Class<?> type, Type genericType, Annotation[] annotations,
      boolean acceptPost, boolean multipart) {
    Param param = buildparam0(type, genericType, annotations, acceptPost, multipart);

    if (param != null) {
      DefaultValue df = Reflect.getAnnotation(annotations, DefaultValue.class);
      if (df != null) {
        param.setDefaultValue(df.value());
      }

      Encoded encode = Reflect.getAnnotation(annotations, Encoded.class);
      if (encode != null && param instanceof ParsableParam) {
        ((ParsableParam) param).setEncode(true);
      }
    }
    return param;
  }

  private Param buildparam0(final Class<?> type, Type genericType, Annotation[] annotations,
      boolean acceptPost, boolean multipart) {

    for (int i = 0; i < annotations.length; i++) {
      ParamBuilder pb = paramBuilders.get(annotations[i].annotationType());
      if (pb != null) {
        return pb.build(type, genericType, annotations, multipart, annotations[i]);
      }
    }

    if (acceptPost) {
      // data from post
      return new net.gcolin.rest.param.PostParam(type, genericType, annotations);
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType,
      Annotation[] annotations) {
    if (rawType == String.class) {
      return null;
    }
    for (int i = list.size() - 1; i >= 0; i--) {
      ParamConverter<?> pc = list.get(i).getConverter(rawType, genericType, annotations);
      if (pc != null) {
        return (ParamConverter<T>) pc;
      }
    }
    throw new IllegalArgumentException("cannot find ParamConverter for " + genericType);
  }

}
