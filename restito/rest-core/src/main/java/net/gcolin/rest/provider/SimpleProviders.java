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

package net.gcolin.rest.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Supplier;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import net.gcolin.common.Priority;
import net.gcolin.common.collection.Collections2;
import net.gcolin.common.reflect.Priorities;
import net.gcolin.common.reflect.Reflect;
import net.gcolin.common.reflect.TypedInvocationHandler;
import net.gcolin.rest.BindingEnvironment;
import net.gcolin.rest.Environment;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.param.HttpHeadersParam;
import net.gcolin.rest.param.LocaleParam;
import net.gcolin.rest.param.LocaleProviderParam;
import net.gcolin.rest.param.RequestParam;
import net.gcolin.rest.param.SecurityContextParam;
import net.gcolin.rest.param.SingletonParam;
import net.gcolin.rest.param.UriInfoParam;
import net.gcolin.rest.util.ReflectRest;

/**
 * the Providers implementation.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class SimpleProviders
    implements MessageBodyWriter<Object>, MessageBodyReader<Object>, Providers {

  private MessageBodyWriter<?>[] writers;
  private MessageBodyReader<?>[] readers;
  private Map<Class<?>, ExceptionMapper<Throwable>> exceptionMappers = new HashMap<>();
  private Set<Class<?>> exceptionMappersExcluded = new HashSet<>();
  private Map<Class<?>, Map<MediaType, Object>> contextResolvers = new HashMap<>();
  private BindingEnvironment<Supplier<Object>> contextProviders = new BindingEnvironment<>();
  private BindingEnvironment<Supplier<Object>> contextMetaProviders = new BindingEnvironment<>();
  private RuntimeType runtimeType;

  public SimpleProviders(RuntimeType runtimeType) {
    this.runtimeType = runtimeType;
  }

  public BindingEnvironment<Supplier<Object>> getContextMetaProviders() {
    return contextMetaProviders;
  }

  public BindingEnvironment<Supplier<Object>> getContextProviders() {
    return contextProviders;
  }

  /**
   * Load providers.
   */
  public void load() {
    contextProviders.getBinding().clear();
    contextProviders.bind(UriInfo.class, new SingletonSupplier<>(new UriInfoParam()));
    contextProviders.bind(HttpHeaders.class, new SingletonSupplier<>(new HttpHeadersParam()));
    contextProviders.bind(Request.class, new SingletonSupplier<>(new RequestParam()));
    contextProviders.bind(SecurityContext.class,
        new SingletonSupplier<>(new SecurityContextParam()));
    contextProviders.bind(Providers.class, new SingletonSupplier<>(new SingletonParam(this)));
    contextProviders.bind(Locale.class, new SingletonSupplier<>(new LocaleParam()));
    contextMetaProviders.getBinding().clear();
    contextMetaProviders.bind(Locale.class, new SingletonSupplier<>(new LocaleProviderParam()));

    contextResolvers.clear();
    exceptionMappers.clear();
    exceptionMappersExcluded.clear();

    this.writers = load0(MessageBodyWriter.class);
    this.readers = load0(MessageBodyReader.class);

    for (Configurator c : ServiceLoader.load(Configurator.class)) {
      c.configure(this);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T[] load0(Class<T> type) {
    List<T> wlist = new ArrayList<>();
    for (T w : ServiceLoader.load(type)) {
      ConstrainedTo ct = w.getClass().getAnnotation(ConstrainedTo.class);
      if (ct == null || ct.value() == runtimeType) {
        wlist.add(w);
      }
    }
    return wlist.toArray((T[]) Array.newInstance(type, wlist.size()));
  }

  /**
   * Decorate the providers.
   * 
   * @param env the bean provider
   */
  @SuppressWarnings("unchecked")
  public void flush(Environment env) {
    env.setProviders(this);
    
    Priorities.sortArray(writers, a -> a.getClass().getAnnotation(Priority.class));
    Priorities.sortArray(readers, a -> a.getClass().getAnnotation(Priority.class));
    
    for (int i = 0; i < this.writers.length; i++) {
      writers[i] = (MessageBodyWriter<?>) env.decorate(writers[i], MessageBodyWriter.class);
    }
    for (int i = 0; i < this.readers.length; i++) {
      readers[i] = (MessageBodyReader<?>) env.decorate(readers[i], MessageBodyReader.class);
    }
    
    exceptionMappers.replaceAll((type, ex) -> env.decorate(ex, ExceptionMapper.class));
    exceptionMappersExcluded.clear();
  }

  /**
   * Add a context resolver.
   * 
   * @param resolver a context resolver instance
   * @param resolverType a context resolver type
   */
  public void add(ContextResolver<?> resolver, Class<?> resolverType) {
    Map<MediaType, Object> cr = contextResolvers.get(resolverType);
    if (cr == null) {
      cr = new HashMap<>();
      contextResolvers.put(resolverType, cr);
    }
    Class<?> type = TypedInvocationHandler.getRealType(resolver);
    List<FastMediaType> list = ReflectRest.buildMediaType(type.getAnnotation(Produces.class));
    if (list.isEmpty()) {
      cr.put(null, resolver);
    } else {
      for (FastMediaType md : list) {
        cr.put(md.isWildcard() ? null : md, resolver);
      }
    }
  }

  public void add(MessageBodyReader<?> reader) {
    this.readers = Collections2.addToArray(this.readers, reader);
  }

  public void add(MessageBodyWriter<?> writer) {
    this.writers = Collections2.addToArray(this.writers, writer);
  }

  /**
   * Add an ExceptionMapper.
   * 
   * @param exceptionMapper an instance
   */
  public void add(ExceptionMapper<Throwable> exceptionMapper) {
    Class<?> type = TypedInvocationHandler.getRealType(exceptionMapper);
    Class<?> exType = Throwable.class;

    for(Method method: type.getMethods()) {
    	if(method.getName().equals("toResponse") && method.getParameterCount() == 1) {
    		Type t = method.getGenericParameterTypes()[0];
    		if(t instanceof TypeVariable) {
    			exType = Reflect.toClass(Reflect.getType(ExceptionMapper.class, type, t));
    		} else {
    			exType = Reflect.toClass(t);
    		}
    		break;
    	}
    }
    exceptionMappers.put(exType, exceptionMapper);
    exceptionMappersExcluded.clear();
  }

  /**
   * Add a provider.
   * 
   * @param component a provider instance
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void add(Supplier<?> component) {
    Class<?> clazz = (Class) TypedInvocationHandler.getRealType(component);
    Class<?> cl = (Class<?>) Reflect
        .getTypeArguments(Supplier.class, clazz, clazz.getEnclosingClass())
        .get(0);
    contextProviders.bind(cl, (Supplier<Object>) component,
        contextProviders.findQualifiers(component.getClass().getAnnotations()));
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return type == Response.class;
  }

  @Override
  public long getSize(Object entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return entity == null || (entity instanceof Response && ((Response) entity).getEntity() == null)
        ? 0 : -1;
  }

  @Override
  public void writeTo(Object entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException {
    if (entity == null) {
      return;
    }
    if (entity instanceof Response) {
      Response resp = (Response) entity;
      if (resp.getEntity() != null) {
        Class<?> typee = resp.getEntity().getClass();
        writeTo0(resp.getEntity(), typee, typee, annotations, mediaType, httpHeaders, entityStream);
      }
    } else {
      writeTo0(entity, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

  }

  @SuppressWarnings("unchecked")
  private void writeTo0(Object entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
      throws IOException {
    MessageBodyWriter<Object> writer =
        (MessageBodyWriter<Object>) getMessageBodyWriter(type, genericType, annotations, mediaType);
    writer.writeTo(entity, type, genericType, annotations, mediaType, httpHeaders, entityStream);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    for (int i = 0, l = readers.length; i < l; i++) {
      if (readers[i].isReadable(type, genericType, annotations, mediaType)) {
        return (MessageBodyReader<T>) readers[i];
      }
    }
    throw new ProcessingException(
        "cannot find MessageBodyReader for " + genericType + " with mediatype " + mediaType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    if (type == Response.class) {
      return (MessageBodyWriter<T>) this;
    }
    if(genericType == null) {
      genericType = type;
    }
    for (int i = 0, l = writers.length; i < l; i++) {
      if (writers[i].isWriteable(type, genericType, annotations, mediaType)) {
        return (MessageBodyWriter<T>) writers[i];
      }
    }
    throw new ProcessingException("cannot find MessageBodyWriter for " + genericType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized <T extends Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
	  ExceptionMapper<T> result = (ExceptionMapper<T>) exceptionMappers.get(type);
	  if(result == null) {
		  for(Entry<Class<?>, ExceptionMapper<Throwable>> t : exceptionMappers.entrySet()) {
			  if(t.getKey().isAssignableFrom(type)) {
				  exceptionMappers.put(type, t.getValue());
				  result = (ExceptionMapper<T>) t.getValue();
				  break;
			  }
		  }
		  if(result == null) {
			  exceptionMappersExcluded.add(type);
		  }
	  }
	  return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> ContextResolver<T> getContextResolver(Class<T> contextType, MediaType mediaType) {
    Map<MediaType, Object> map = contextResolvers.get(contextType);
    if (map == null) {
      return null;
    }
    Object obj = map.get(mediaType);
    if (obj == null && mediaType != null) {
      obj = map.get(FastMediaType.valueOf(mediaType.getType() + "/*"));
    }
    return (ContextResolver<T>) (obj == null ? map.get(null) : obj);
  }

  public boolean isMediatypeContextResolver(Class<?> contextType) {
    Map<MediaType, Object> map = contextResolvers.get(contextType);
    return map != null && (map.size() > 1 || map.get(null) == null);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return true;
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
      throws IOException, WebApplicationException {
    return getMessageBodyReader(type, genericType, annotations, mediaType).readFrom(type,
        genericType, annotations, mediaType, httpHeaders, entityStream);
  }

}
