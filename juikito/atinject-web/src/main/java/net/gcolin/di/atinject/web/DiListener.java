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
package net.gcolin.di.atinject.web;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import net.gcolin.di.atinject.Environment;
import net.gcolin.di.atinject.Extension;
import net.gcolin.di.atinject.Instance;
import net.gcolin.di.core.Key;

/**
 * A listener for initializing the DI. Allow injection of ServletContext and
 * ServletRequest.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class DiListener implements ServletContextListener, HttpSessionListener, ServletRequestListener {

	private final ThreadLocal<ServletRequest> request = new ThreadLocal<>();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		long start = System.currentTimeMillis();
		Environment env = new Environment(sce.getServletContext().getClassLoader()) {

			@Override
			protected List<Extension> loadExtensions() {
				List<Extension> extensions = super.loadExtensions();
				for (Extension extension : extensions) {
					if (extension.getClass() == WebExtension.class) {
						WebExtension webExtension = (WebExtension) extension;
						webExtension.setContextProvider(() -> sce.getServletContext());
						webExtension.setRequestProvider(() -> request.get());
					}
				}
				return extensions;
			}

		};
		sce.getServletContext().setAttribute(DiFilter.DIENV, env);
		sce.getServletContext().setAttribute("inject", new BiFunction<String, Type, Object>() {

			@Override
			public Object apply(String name, Type type) {
				return env.find(name);
			}

		});
		sce.getServletContext().setAttribute("injector", new Consumer<Object>() {

			@Override
			public void accept(Object obj) {
				env.bind(obj);
			}
		});
		sce.getServletContext().setAttribute("uninjector", new Consumer<Object>() {

			@Override
			public void accept(Object obj) {
				env.unbind(obj);
			}
		});
		env.start();
		env.getLog().log(Level.INFO, "start dependency injection in {0}ms", System.currentTimeMillis() - start);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		Environment env = (Environment) sce.getServletContext().getAttribute(DiFilter.DIENV);
		destroy(env, (Map<Key, Instance>) sce.getServletContext().getAttribute(DiFilter.DI_INSTANCES));
		env.stop();
	}

	@Override
	public void sessionCreated(HttpSessionEvent se) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		destroy((Environment) se.getSession().getServletContext().getAttribute(DiFilter.DIENV),
				(Map<Key, Instance>) se.getSession().getAttribute(DiFilter.DI_INSTANCES));
	}

	static void destroy(Environment env, Map<Key, Instance> list) {
		if (list != null) {
			try {
				for (Instance run : list.values()) {
					run.destroy(env);
				}
			} catch (Exception ex) {
				env.getLog().log(Level.SEVERE, "cannot destroy", ex);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void requestDestroyed(ServletRequestEvent sre) {
		DiListener.destroy((Environment) sre.getServletRequest().getServletContext().getAttribute(DiFilter.DIENV),
				(Map<Key, Instance>) sre.getServletRequest().getAttribute(DiFilter.DI_INSTANCES));
		request.remove();
	}

	@Override
	public void requestInitialized(ServletRequestEvent sre) {
		request.set(sre.getServletRequest());
	}

}
