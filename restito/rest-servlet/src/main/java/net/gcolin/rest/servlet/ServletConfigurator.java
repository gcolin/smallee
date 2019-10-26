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

package net.gcolin.rest.servlet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.FeatureContext;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.param.Param;
import net.gcolin.rest.parambuilder.ParamBuilder;
import net.gcolin.rest.provider.Configurator;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.provider.SingletonSupplier;
import net.gcolin.rest.server.ServerProviders;
import net.gcolin.rest.util.Router;

/**
 * Custom configuration.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServletConfigurator implements Configurator {

  @Override
  public void configure(SimpleProviders provider) {
    if (provider instanceof ServerProviders) {
      provider.getContextProviders().bind(HttpServletRequest.class,
          new SingletonSupplier<>(new HttpServletRequestParam()));
      provider.getContextProviders().bind(HttpServletResponse.class,
          new SingletonSupplier<>(new HttpServletResponseParam()));
      provider.getContextProviders().bind(ServletConfig.class,
          new SingletonSupplier<>(new ServletConfigParam()));
      provider.getContextProviders().bind(ServletContext.class,
          new SingletonSupplier<>(new ServletContextParam()));
      provider.getContextProviders().bind(Router.class, new SingletonSupplier<>(new RouterParam()));

      ServerProviders sprovider = (ServerProviders) provider;
      sprovider.getParamBuilders().put(FormParam.class, new FormParamBuilder(sprovider));
    }
  }

  private static class FormParamBuilder implements ParamBuilder {

    private ServerProviders provider;

    public FormParamBuilder(ServerProviders provider) {
      this.provider = provider;
    }

    @Override
    public Param build(Class<?> type, Type genericType, Annotation[] annotations, boolean multipart,
        Annotation annotation) {
      FormParam form = (FormParam) annotation;
      if (multipart) {
        if (type == Part.class) {
          return new PartParam(form.value());
        } else if (isPartCollection(type, genericType)) {
          return new PartsParam();
        } else {
          QueryMultipartParam param = new QueryMultipartParam(form.value());
          param.setBoxer(provider.getConverter(type, genericType, annotations));
          return param;
        }
      } else {
        net.gcolin.rest.param.QueryParam param = new net.gcolin.rest.param.QueryParam(form.value());
        param.setBoxer(provider.getConverter(type, genericType, annotations));
        return param;
      }
    }

    private boolean isPartCollection(Class<?> type, Type genericType) {
      return type == Collection.class
          && Reflect.getTypeArguments(Collection.class, genericType, null).get(0) == Part.class;
    }
  }

  @Override
  public void configureFeature(FeatureContext fb) {
    // nothing to do
  }


}
