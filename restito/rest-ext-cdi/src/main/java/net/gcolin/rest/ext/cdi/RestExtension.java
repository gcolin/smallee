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

package net.gcolin.rest.ext.cdi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.annotation.MultipartConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.Logs;
import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.servlet.RestServlet;

/**
 * The CDI extension.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class RestExtension implements Extension {

	private List<AnnotatedType<?>> apps = new ArrayList<AnnotatedType<?>>();
	private Map<String, RestServlet> servlets = new HashMap<>();

	void processAnnotatedType(@Observes ProcessAnnotatedType<?> event) {
		ApplicationPath app = event.getAnnotatedType().getAnnotation(ApplicationPath.class);
		if (app != null && Application.class.isAssignableFrom(event.getAnnotatedType().getJavaClass())) {
			apps.add(event.getAnnotatedType());
			event.veto();
		}
		if (event.getAnnotatedType().isAnnotationPresent(Provider.class)) {
			event.veto();
		}
	}

	@Produces
	public ContainerRequestContext getContainerRequestContext() {
		return Contexts.instance().get();
	}

	public void startup(ServletContext sc) {
		CDI<Object> cdi = CDI.current();
		BeanManager bm = cdi.getBeanManager();
		for (AnnotatedType<?> appType : apps) {
			try {
				String path = appType.getJavaClass().getAnnotation(ApplicationPath.class).value();
				if (!path.startsWith("/")) {
					path = "/" + path;
				}
				RestServlet servlet = servlets.get(path);
				Application app = (Application) Reflect.newInstance(appType.getJavaClass());
				if (servlet == null) {
					servlet = new RestServlet();
					servlet.env(new CdiEnvironment(bm));
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
				Logs.LOG.info("start rest app {} in the context {}/*", appType.getJavaClass().getName(), path);
			} catch (Exception ex) {
				Logs.LOG.error("cannot add rest application " + appType.getJavaClass().getName(), ex);
			}

		}

		for (RestServlet servlet : servlets.values()) {
			servlet.initialize();
		}
	}
}
