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
package net.gcolin.di.atinject.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.core.InjectException;
import net.gcolin.rest.servlet.RestServlet;

/**
 * Enable Restito with annotation ApplicationPath.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class RestExtension implements Extension {

  @Override
  public void doStart(Environment env) {}

  @Override
  public void doStarted(Environment environment) {
    Logger log = Logger.getLogger(this.getClass().getName());
    Map<String, RestServlet> servlets = new HashMap<>();
    for (Class<?> clazz : environment.getBeanClasses()) {
      if (Application.class.isAssignableFrom(clazz)) {
        ApplicationPath apppath = clazz.getAnnotation(ApplicationPath.class);
        if (apppath == null) {
          log.info(clazz
              + " is an javax.ws.rs.core.Application and does not have @ApplicationPath: ignore it");
          continue;
        }
        ServletContext sc = environment.get(ServletContext.class);
        String path = apppath.value();
        if (!path.startsWith("/")) {
          path = "/" + path;
        }
        RestServlet servlet = servlets.get(path);
        Application app = (Application) Reflect.newInstance(clazz);
        try {
          if (servlet == null) {
            servlet = new RestServlet();
            servlet.env(new AtInjectEnvironment(environment));
            servlet.app(app, false);
            Dynamic restServlet = sc.addServlet("rest@" + path, servlet);
            restServlet.addMapping(path + "/*");
            restServlet.addMapping(path);
            restServlet.setMultipartConfig(
                new MultipartConfigElement(RestServlet.class.getAnnotation(MultipartConfig.class)));
            restServlet.setLoadOnStartup(1);
            servlets.put(path, servlet);
          } else {
            servlet.app(app, false);
          }
        } catch (ServletException ex) {
          throw new InjectException(ex);
        }
        log.log(Level.INFO, "start rest app {0} in the context {1}/*", new Object[] {clazz.getName(), path});
      }
    }
  }

}
