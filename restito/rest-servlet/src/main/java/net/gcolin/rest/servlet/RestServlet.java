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

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.logging.Level;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.RuntimeDelegate;

import net.gcolin.common.io.ByteArrayOutputStream;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.rest.Environment;
import net.gcolin.rest.FeatureBuilder;
import net.gcolin.rest.Logs;
import net.gcolin.rest.RestConfiguration;
import net.gcolin.rest.RuntimeDelegateImpl;
import net.gcolin.rest.param.SingletonParam;
import net.gcolin.rest.provider.Configurator;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.provider.SingletonSupplier;
import net.gcolin.rest.server.AbstractResource;
import net.gcolin.rest.server.Builder;
import net.gcolin.rest.server.Contexts;
import net.gcolin.rest.server.ResourceArray;
import net.gcolin.rest.server.ResourceSelector;
import net.gcolin.rest.server.RestContainer;
import net.gcolin.rest.server.ServerFeatureBuilder;
import net.gcolin.rest.server.ServerInvocationContext;
import net.gcolin.rest.server.ServerProviders;
import net.gcolin.rest.server.ServerResponse;
import net.gcolin.rest.util.Router;
import net.gcolin.rest.util.RouterResponse;

/**
 * Servlet for dispatching requests to Rest.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@MultipartConfig(fileSizeThreshold = 100 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 20 * 1024 * 1024)
public class RestServlet implements RestContainer, Servlet {

	private static final String JUIKITO_ENV = "di.env";
	private Router<ResourceArray> router;
	private ServerProviders providers = new ServerProviders();
	private Environment env = new Environment();
	private ServiceStrategy strategy = new SimpleServiceStrategy();
	private List<Application> apps = new ArrayList<>();
	private String alias;
	private ServletConfig config;
	private boolean dirty;
	private long start;

	public Builder newResource() {
		return new Builder();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		this.config = config;
		String envName = config.getInitParameter(JUIKITO_ENV);
		env(envName);

		String application = config.getInitParameter("javax.ws.rs.Application");

		if (application != null) {
			try {
				app((Application) env.getProvider(env.getClassLoader().loadClass(application), Application.class, false)
						.get());
			} catch (ClassNotFoundException ex) {
				throw new ServletException("cannot find " + application, ex);
			}
		} else if (apps.isEmpty()) {
			Logs.LOG.warning("cannot find jax rs application");
		}
	}

	public Router<ResourceArray> getRouter() {
		return router;
	}

	public void clear() {
		apps.clear();
		initialize();
	}

	public RestServlet app(Application app) throws ServletException {
		return app(app, true);
	}

	/**
	 * Add an Application.
	 * 
	 * @param app  the Application to add
	 * @param init force initialize
	 * @return the current servlet
	 * @throws ServletException if an error occurs.
	 */
	public RestServlet app(Application app, boolean init) throws ServletException {
		if (app.getProperties().containsKey(JUIKITO_ENV)) {
			Object envConfig = app.getProperties().get(JUIKITO_ENV);
			if (envConfig instanceof Environment) {
				env = (Environment) envConfig;
			} else {
				env((String) envConfig);
			}
		}
		this.apps.add(app);
		dirty = true;
		if (init) {
			initialize();
		}
		return this;
	}

	/**
	 * Remove an Application.
	 * 
	 * @param app an Application
	 * @return true is there are other apps.
	 */
	public boolean removeApp(Application app) {
		apps.remove(app);
		dirty = true;
		return !apps.isEmpty();
	}

	/**
	 * Deploy applications.
	 */
	public synchronized void initialize() {
		if (dirty) {
			start = System.currentTimeMillis();
			RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
			router = new Router<>();
			env.setProviders(providers);
			providers.load(env);
			providers.getContextProviders().bind(Application.class,
					new SingletonSupplier<>(new SingletonParam(new CompositeApplication(apps))));
			FeatureBuilder featureBuilder = new ServerFeatureBuilder(this, providers, router,
					new RestConfiguration(RuntimeType.SERVER), env);
			for (int i = 0; i < apps.size(); i++) {
				Application app = apps.get(i);
				for (Object singleton : app.getSingletons()) {
					featureBuilder.register(env.decorate(singleton));
				}
			}
			for (int i = 0; i < apps.size(); i++) {
				Application app = apps.get(i);
				for (Class<?> c : app.getClasses()) {
					featureBuilder.register(c);
				}
			}

			for (Configurator c : ServiceLoader.load(Configurator.class)) {
				c.configureFeature(featureBuilder);
			}

			featureBuilder.build();
			providers.flush(env);

			Logs.LOG.log(Level.INFO, "start jax rs application : {0} in {1}ms",
					new Object[] { apps, System.currentTimeMillis() - start });

			if (Logs.LOG.isLoggable(Level.FINE)) {
				Logs.LOG.fine(router.toString());
			}

			dirty = false;
		}
	}

	/**
	 * Set the Environment by class name.
	 * 
	 * @param envName class name of the environment
	 * @return the current servlet
	 * @throws ServletException if an error occurs.
	 */
	public RestServlet env(String envName) throws ServletException {
		if (envName == null && this.env == null) {
			return env(new Environment());
		} else if (envName != null) {
			try {
				return env((Environment) Reflect.newInstance(this.getClass().getClassLoader().loadClass(envName)));
			} catch (ClassNotFoundException ex) {
				throw new ServletException("cannot find " + envName, ex);
			}
		}
		return this;
	}

	/**
	 * Set the Environment.
	 * 
	 * @param env the Environment.
	 * @return the current servlet
	 */
	public RestServlet env(Environment env) {
		if (env != this.env) {
			this.env = env;
			this.dirty = true;
		}
		return this;
	}

	@Override
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		if (dirty) {
			initialize();
		}

		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;

		ServerInvocationContext ctx = new ServerInvocationContext(new ServletExchange(request, response, this));

		ThreadLocal<ServerInvocationContext> tlocal = Contexts.instance();
		tlocal.set(ctx);
		try {
			strategy.service(request, response, ctx);
		} finally {
			tlocal.remove();
		}
	}

	private void handleResource(ServerInvocationContext context) throws IOException {
		ServletExchange sex = (ServletExchange) context.getExchange();
		ByteArrayOutputStream bout = null;
		try {
			ServerResponse response = (ServerResponse) context.getResource().handle(context);
			if (sex.hasWritten()) {
				return;
			}

			int status = response.getStatus();
			if (status >= HttpURLConnection.HTTP_BAD_REQUEST && !response.hasEntity()) {
				sex.getResponse().sendError(response.getStatus());
			} else if (shouldUpdateStatus(sex, status)) {
				sex.getResponse().setStatus(status);
			}

			if (response.getEntity() != null) {

				AbstractResource resource = context.getResource();
				bout = new ByteArrayOutputStream();

				if (resource.getWriterDecorator() == null) {
					context.getWriter().writeTo(response.getEntity(), context.getEntityClass(),
							context.getEntityGenericType(), response.getAllAnnotations(), context.getProduce(),
							response.newContext().getHeaders(), bout);
				} else {
					resource.getWriterDecorator().writeTo(context, response.getEntity(), context.getEntityClass(),
							context.getEntityGenericType(), response.getAllAnnotations(),
							response.newContext().getHeaders(), bout);
				}

			}
			writeHeaders(context.getProduce(), sex.getResponse(), response.getStringHeaders(), bout);
		} catch (NoContentException ex) {
			if (!tryExceptionMapper(sex.getResponse(), new BadRequestException(ex))) {
				sex.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST);
			}
		} catch (RuntimeException ex) {
			Throwable cause = ex.getCause();
			if (ex.getClass() != WebApplicationException.class) {
				cause = ex;
			}
			boolean done = false;
			if (cause != null) {
				done = tryExceptionMapper(sex.getResponse(), cause);
			}
			if (!done) {
				done = tryExceptionMapper(sex.getResponse(), ex);
			}

			if (!done) {
				Logs.LOG.log(Level.SEVERE, "cannot execute " + context.getResource().getResourceMethod(), ex);

				if (ex instanceof WebApplicationException && ((WebApplicationException) ex).getResponse() != null) {
					sendResponse(sex.getResponse(), ((WebApplicationException) ex).getResponse(), providers);
					return;
				}
				if (ex.getMessage() != null) {
					sex.getResponse().getOutputStream().println(ex.getMessage());
				}
				sex.getResponse().sendError(HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
		} finally {
			if (bout != null) {
				bout.release();
			}
		}
	}

	private boolean shouldUpdateStatus(ServletExchange sex, int status) {
		return status != HttpURLConnection.HTTP_OK && status > 0
				&& (sex.getResponse().getStatus() == 0 || sex.getResponse().getStatus() == HttpURLConnection.HTTP_OK);
	}

	/**
	 * Write headers to an HttpServletResponse.
	 * 
	 * @param responseMediaType output media type
	 * @param response          servlet response
	 * @param headers           map of headers
	 * @param bout              transient buffer of output
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeHeaders(MediaType responseMediaType, HttpServletResponse response,
			MultivaluedMap<String, String> headers, ByteArrayOutputStream bout) throws IOException {
		if (headers != null && !headers.isEmpty()) {
			for (Entry<String, List<String>> entry : headers.entrySet()) {
				for (String e : entry.getValue()) {
					response.addHeader(entry.getKey(), e);
				}
			}
		}
		if (responseMediaType != null) {
			response.setHeader(HttpHeaders.CONTENT_TYPE, responseMediaType.toString());
		}

		if (bout != null) {
			if (!bout.isEmpty()) {
				response.setHeader(HttpHeaders.CONTENT_LENGTH, bout.getSize() + "");
				bout.writeTo(response.getOutputStream());
			}
		} else {
			response.setHeader(HttpHeaders.CONTENT_LENGTH, "0");
		}
	}

	@SuppressWarnings("unchecked")
	protected boolean tryExceptionMapper(HttpServletResponse response, Throwable cause) throws IOException {
		ExceptionMapper<Throwable> mapper = (ExceptionMapper<Throwable>) providers.getExceptionMapper(cause.getClass());
		if (mapper != null) {
			Response resp = mapper.toResponse(cause);
			sendResponse(response, resp, providers);
			return true;
		}
		return false;
	}

	interface ServiceStrategy {
		void service(HttpServletRequest req, HttpServletResponse res, ServerInvocationContext ctx) throws IOException;
	}

	class PreMatchingServiceStrategy implements ServiceStrategy {

		List<ContainerRequestFilter> filters = new ArrayList<>();
		ServiceStrategy delegate;

		@Override
		public void service(HttpServletRequest req, HttpServletResponse response, ServerInvocationContext ctx)
				throws IOException {

			for (ContainerRequestFilter filter : filters) {
				filter.filter(ctx);
				if (ctx.getAbortResponse() != null) {
					Response resp = ctx.getAbortResponse();
					sendResponse(response, resp, providers);
					return;
				}
			}

			delegate.service(req, response, ctx);

		}

	}

	class SimpleServiceStrategy implements ServiceStrategy {

		@Override
		public void service(HttpServletRequest request, HttpServletResponse response, ServerInvocationContext ctx)
				throws IOException {
			String pathInfo = request.getPathInfo();
			if (pathInfo == null) {
				pathInfo = "";
			}
			RouterResponse<ResourceArray> resp = router.get(pathInfo, pathInfo.isEmpty() ? 0 : 1,
					new RouterResponse<ResourceArray>());

			if (resp != null) {
				ResourceSelector selector = resp.getResult().get(request.getMethod());
				if (selector == null) {
					if (!tryExceptionMapper(response,
							new NotAllowedException((Throwable) null, resp.getResult().getAlloweds()))) {
						response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
					}
				} else {
					AbstractResource resource = selector.select(ctx);
					if (resource == null) {
						response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
					} else {
						ctx.setResource(resource);
						ctx.setParams(resp.getParams());
						handleResource(ctx);
					}
				}
			} else if (!tryExceptionMapper(response, new NotFoundException())) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}

	}

	/**
	 * Send the response to the client.
	 * 
	 * @param response  servlet response
	 * @param resp      rest response
	 * @param providers rest providers
	 * @throws IOException if an I/O error occurs.
	 */
	@SuppressWarnings("unchecked")
	public static void sendResponse(HttpServletResponse response, Response resp, SimpleProviders providers)
			throws IOException {
		if (!resp.hasEntity() && resp.getStatus() >= 400) {
			response.sendError(resp.getStatus());
			return;
		}

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			response.setStatus(resp.getStatus());
			if (resp.hasEntity()) {
				Type genericType;
				Class<Object> type;
				if (resp.getEntity() instanceof GenericEntity) {
					GenericEntity<?> genericEntity = (GenericEntity<?>) resp.getEntity();
					genericType = genericEntity.getType();
					type = (Class<Object>) genericEntity.getRawType();
				} else {
					type = (Class<Object>) resp.getEntity().getClass();
					genericType = type;
				}
				MessageBodyWriter<Object> mw = providers.getMessageBodyWriter(type, genericType,
						((ServerResponse) resp).getAllAnnotations(), resp.getMediaType());

				mw.writeTo(resp.getEntity(), type, genericType, ((ServerResponse) resp).getAllAnnotations(),
						resp.getMediaType(), ((ServerResponse) resp).getHeaders(), bout);
			}
			writeHeaders(resp.getMediaType(), response, resp.getStringHeaders(), bout);
		} finally {
			bout.release();
		}
	}

	@Override
	public void addPreMatchingFilter(ContainerRequestFilter filter) {
		if (!(strategy instanceof PreMatchingServiceStrategy)) {
			PreMatchingServiceStrategy prematchingStrategy = new PreMatchingServiceStrategy();
			prematchingStrategy.delegate = strategy;
			strategy = prematchingStrategy;
		}
		PreMatchingServiceStrategy prematchingStrategy = (PreMatchingServiceStrategy) strategy;
		prematchingStrategy.filters.add(filter);
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public ServletConfig getServletConfig() {
		return config;
	}

	@Override
	public String getServletInfo() {
		return "juikito rest servlet with " + apps;
	}

	@Override
	public void destroy() {
	}
}
