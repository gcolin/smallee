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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.WriterInterceptor;

import net.gcolin.rest.Environment;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.MessageBodyReaderDecorator;
import net.gcolin.rest.MessageBodyWriterDecorator;
import net.gcolin.rest.param.Param;
import net.gcolin.rest.param.PostParam;
import net.gcolin.rest.provider.Provider;
import net.gcolin.rest.provider.SimpleProviders;
import net.gcolin.rest.util.Filters;
import net.gcolin.rest.util.HasPath;
import net.gcolin.rest.util.HttpHeader;

/**
 * A resource that calls the service with a java method.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class Resource extends AbstractResource implements HasPath, ResourceInfo {

  private String path;
  private Method method;
  private FastMediaType[] accept;
  private FastMediaType[] acceptCompatibility = new FastMediaType[0];
  private boolean[] isAcceptCompatibility = new boolean[0];
  @SuppressWarnings("unchecked")
  private MessageBodyWriter<Object>[] writers = new MessageBodyWriter[0];

  private FastMediaType[] consume;
  private FastMediaType[] consumeCompatibility = new FastMediaType[0];
  private boolean[] isConsumeCompatibility = new boolean[0];
  @SuppressWarnings("unchecked")
  private MessageBodyReader<Object>[] readers = new MessageBodyReader[0];
  private PostParam postParam;
  private boolean isvoid;
  private Supplier<Object> instance;
  private Param[] params;
  private Class<?> source;
  private Annotation[] annotations;
  private Type genericType;
  private Class<?> type;
  private Providers providers;
  private MessageBodyWriterDecorator writerDecorator;
  private List<ContainerResponseFilter> responseFilters;
  private ResponseStrategy responseStrategy;
  private Set<String> allowedMethods;
  private Consumer<Object[]> paramValidator;

  /**
   * Create a resource.
   * 
   * @param method the method of the resource
   * @param accept the accepted media types
   * @param params the parameters of the methods
   * @param consume the accepted media types of input
   * @param source the class of the resource
   * @param providers the providers
   */
  public Resource(Method method, List<FastMediaType> accept, Param[] params,
      Set<FastMediaType> consume, Class<?> source, SimpleProviders providers) {
    this.providers = providers;
    this.method = method;
    genericType = method.getGenericReturnType();
    type = method.getReturnType();

    isvoid = method.getReturnType() == Void.TYPE;
    if (method.getReturnType() == Void.TYPE) {
      responseStrategy = new VoidResponseStrategy();
    } else if (method.getReturnType() == Response.class) {
      responseStrategy = new ResponseResponseStrategy();
    } else if (method.getReturnType() == GenericEntity.class) {
      responseStrategy = new GenericEntityResponseStrategy();
    } else {
      responseStrategy = new DefaultResponseStrategy();
    }

    if (!isvoid && accept.isEmpty()) {
      accept.add(FastMediaType.valueOf(MediaType.WILDCARD));
    }

    this.accept = new FastMediaType[accept.size()];
    accept.toArray(this.accept);
    setParams(params);
    this.source = source;
    annotations = method.getAnnotations();
    if (params != null) {
      setPostParam(consume);
    }
    if (!consume.isEmpty()) {
      this.consume = new FastMediaType[consume.size()];
      consume.toArray(this.consume);
    }
  }

  @Override
  public MessageBodyWriterDecorator getWriterDecorator() {
    return writerDecorator;
  }

  @Override
  public List<ContainerResponseFilter> getResponseFilters() {
    return responseFilters;
  }

  @Override
  public void setParamValidator(Consumer<Object[]> paramValidator) {
    this.paramValidator = paramValidator;
  }

  public Providers getProviders() {
    return providers;
  }

  /**
   * Set the singleton.
   * 
   * @param singleton the singleton object of resource
   * @param env the bean provider used if the singleton is {@code null}
   */
  public void setSingleton(final Object singleton, Environment env) {
    if (singleton != null) {
      instance = () -> singleton;
    } else {
      try {
        instance = env.getProvider(source, source, false);
      } catch (Exception e1) {
        throw new ProcessingException(e1);
      }
    }
  }

  private void setPostParam(Set<FastMediaType> consume) {
    for (int i = 0; i < params.length; i++) {
      if (params[i] instanceof PostParam) {
        postParam = (PostParam) params[i];
      }
    }
    if (postParam != null && consume.isEmpty()) {
      consume.add(FastMediaType.valueOf(MediaType.WILDCARD));
    }
  }

  private void addReader(MessageBodyReader<Object> reader, FastMediaType fastMediaType) {
    if (readers.length <= fastMediaType.getId()) {
      @SuppressWarnings("unchecked")
      MessageBodyReader<Object>[] tmp = new MessageBodyReader[fastMediaType.getId() + 1];
      System.arraycopy(readers, 0, tmp, 0, readers.length);
      readers = tmp;
    }
    readers[fastMediaType.getId()] = reader;
  }

  @SuppressWarnings("unchecked")
  private MessageBodyWriter<Object> addWriter(FastMediaType fastMediaType) {
    Method met = getResourceMethod();
    return addWriter((MessageBodyWriter<Object>) providers.getMessageBodyWriter(met.getReturnType(),
        met.getGenericReturnType(), met.getAnnotations(), fastMediaType), fastMediaType);
  }

  private MessageBodyWriter<Object> addWriter(MessageBodyWriter<Object> writer,
      FastMediaType fastMediaType) {
    if (writers.length <= fastMediaType.getId()) {
      @SuppressWarnings("unchecked")
      MessageBodyWriter<Object>[] tmp = new MessageBodyWriter[fastMediaType.getId() + 1];
      System.arraycopy(writers, 0, tmp, 0, writers.length);
      writers = tmp;
    }
    writers[fastMediaType.getId()] = writer;
    return writer;
  }

  @Override
  public Response handle(ServerInvocationContext context) throws IOException {
    try {
      Object response = method.invoke(instance.get(), createParams(context));

      return responseStrategy.send(context, response);
    } catch (InvocationTargetException ex) {
      Throwable th = ex.getTargetException();
      if (th instanceof WebApplicationException) {
        throw (WebApplicationException) th;
      } else {
        throw new WebApplicationException(th);
      }
    } catch (IllegalAccessException | IllegalArgumentException ex) {
      throw new WebApplicationException("cannot exec " + method, ex);
    }
  }

  private Response writeTo(ServerInvocationContext context, Object entity, Class<?> type,
      Type genericType, MultivaluedMap<String, String> httpHeaders) throws IOException {
    context.setEntityClass(type);
    context.setEntityGenericType(genericType);
    ResponseBuilder rb = Response.status(context.getStatus());
    rb.entity(entity, annotations);

    addCookies(context, rb);

    addHeaders(httpHeaders, rb);

    rb.type(context.getProduce());

    ServerResponse resp = (ServerResponse) rb.build();

    if (responseFilters != null) {
      ContainerResponseContext ctx = resp.newContext();
      List<ContainerResponseFilter> filters = responseFilters;
      for (int i = 0; i < filters.size(); i++) {
        filters.get(i).filter(context, ctx);
      }

      context.setEntityClass(ctx.getEntityClass());
      context.setEntityGenericType(ctx.getEntityType());
      context.setProduce(FastMediaType.valueOf(ctx.getMediaType()));
    }

    FastMediaType responseMediaType = context.getProduce();
    if (responseMediaType != null) {
      context.setWriter(findWriter(responseMediaType));
    }
    return resp;
  }

  private void addHeaders(MultivaluedMap<String, String> httpHeaders, ResponseBuilder rb) {
    if (httpHeaders != null && !httpHeaders.isEmpty()) {
      for (Entry<String, List<String>> e : httpHeaders.entrySet()) {
        for (String v : e.getValue()) {
          rb.header(e.getKey(), v);
        }
      }
    }
  }

  private void addCookies(ServerInvocationContext context, ResponseBuilder rb) {
    if (context.hasNewCookies()) {
      for (NewCookie c : context.getNewCookies().values()) {
        rb.cookie(c);
      }
    }
  }

  private MessageBodyWriter<Object> findWriter(FastMediaType responseMediaType) {
    if (responseMediaType.getId() >= writers.length) {
      addWriter(responseMediaType);
    }
    MessageBodyWriter<Object> writer = writers[responseMediaType.getId()];
    if (writer == null) {
      writer = addWriter(responseMediaType);
    }
    return writer;
  }

  @SuppressWarnings("rawtypes")
  private FastMediaType findConsumeMediatype(ServerInvocationContext context) {
    String contentTypeHeader = context.getHeaderString(HttpHeaders.CONTENT_TYPE);
    if (contentTypeHeader == null) {
      throw new BadRequestException(HttpHeaders.CONTENT_TYPE + " cannot be null");
    }
    FastMediaType consumeMediaType = null;
    FastMediaType contentMediaType = FastMediaType.valueOf(contentTypeHeader);
    if (consumeCompatibility.length > contentMediaType.getId()) {
      consumeMediaType = consumeCompatibility[contentMediaType.getId()];
    }
    if (consumeCompatibility.length <= contentMediaType.getId()
        || !isConsumeCompatibility[contentMediaType.getId()]) {
      if (consumeCompatibility.length <= contentMediaType.getId()) {
        FastMediaType[] tmp = new FastMediaType[contentMediaType.getId() + 1];
        System.arraycopy(consumeCompatibility, 0, tmp, 0, consumeCompatibility.length);
        consumeCompatibility = tmp;

        boolean[] tmp2 = new boolean[contentMediaType.getId() + 1];
        System.arraycopy(isConsumeCompatibility, 0, tmp2, 0, isConsumeCompatibility.length);
        isConsumeCompatibility = tmp2;
      }
      for (int i = 0, l = consume.length; i < l; i++) {
        if (consume[i].isCompatible(contentMediaType)) {
          consumeMediaType = consume[i];
          if (postParam != null) {
            MessageBodyReader<Object> reader = providers.getMessageBodyReader(postParam.getType(),
                postParam.getGenericType(), postParam.getAnnotations(), consumeMediaType);
            if (consumeMediaType.isWildcard() && reader instanceof Provider) {
              consumeMediaType = ((Provider) reader).getDefaultConsumeMediaType();
            }
            addReader(reader, consumeMediaType);
          }
          consumeCompatibility[contentMediaType.getId()] = consumeMediaType;
          break;
        }
      }
      isConsumeCompatibility[contentMediaType.getId()] = true;
    }
    return consumeMediaType;
  }

  private FastMediaType findResponseMediatype(ServerInvocationContext context) {
    FastMediaType responseMediaType = null;
    Iterator<FastMediaType> acceptMediaTypes =
        FastMediaType.iterator(context.getHeaderString(HttpHeaders.ACCEPT));
    while (acceptMediaTypes.hasNext() && responseMediaType == null) {
      FastMediaType acceptMediaType = acceptMediaTypes.next();
      if (acceptCompatibility.length > acceptMediaType.getId()) {
        responseMediaType = acceptCompatibility[acceptMediaType.getId()];
      }
      if (acceptCompatibility.length <= acceptMediaType.getId()
          || !isAcceptCompatibility[acceptMediaType.getId()]) {

        if (acceptCompatibility.length <= acceptMediaType.getId()) {
          FastMediaType[] tmp = new FastMediaType[acceptMediaType.getId() + 1];
          System.arraycopy(acceptCompatibility, 0, tmp, 0, acceptCompatibility.length);
          acceptCompatibility = tmp;

          boolean[] tmp2 = new boolean[acceptMediaType.getId() + 1];
          System.arraycopy(isAcceptCompatibility, 0, tmp2, 0, isAcceptCompatibility.length);
          isAcceptCompatibility = tmp2;
        }

        for (int i = 0, l = accept.length; i < l; i++) {
          if (accept[i].isCompatible(acceptMediaType)) {
            responseMediaType = accept[i];
            if (responseMediaType.isWildcardSubtype()) {
              MessageBodyWriter<Object> writer = findWriter(responseMediaType);
              if (writer instanceof Provider) {
                responseMediaType = ((Provider<Object>) writer).getDefaultProduceMediaType();
              }
            }
            acceptCompatibility[acceptMediaType.getId()] = responseMediaType;
            break;
          }
        }
        isAcceptCompatibility[acceptMediaType.getId()] = true;
      }

    }
    return responseMediaType;
  }

  private Object[] createParams(ServerInvocationContext context) throws IOException {
    if (getParams() != null) {
      Param[] paramsArray = this.getParams();
      Object[] oa = new Object[paramsArray.length];
      for (int i = 0; i < paramsArray.length; i++) {
        oa[i] = paramsArray[i].update(context);
      }
      if (paramValidator != null) {
        paramValidator.accept(oa);
      }
      return oa;
    } else {
      return null;
    }
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public Supplier<Object> getInstance() {
    return instance;
  }

  /**
   * Set the resource path. Internal use only.
   * 
   * @param path the text representation of the path
   */
  public void setPath(String path) {
    if (!path.startsWith("/")) {
      this.path = "/" + path;
    } else {
      this.path = path;
    }
    this.path = path;
  }

  @Override
  public String toString() {
    return path;
  }

  public Param[] getParams() {
    return params;
  }

  public void setParams(Param[] params) {
    this.params = params;
  }

  @Override
  public Class<?> getSource() {
    return source;
  }

  @Override
  public Annotation[] getAnnotations() {
    return annotations;
  }

  @Override
  public Method getResourceMethod() {
    return method;
  }

  @Override
  public Class<?> getResourceClass() {
    return source;
  }

  @Override
  public AbstractResource addInterceptor(WriterInterceptor wi) {
    if (!isvoid) {
      if (writerDecorator == null) {
        writerDecorator = new MessageBodyWriterDecorator();
      }
      writerDecorator.add(wi);
      injectConsumer(wi);
    }
    return this;
  }

  @Override
  public AbstractResource addInterceptor(ReaderInterceptor ri) {
    if (postParam != null) {
      if (postParam.getDecorator() == null) {
        postParam.setDecorator(new MessageBodyReaderDecorator());
      }
      injectConsumer(ri);
      postParam.getDecorator().add(ri);
    }
    return this;
  }

  @Override
  public Set<String> getAllowedMethods() {
    return allowedMethods;
  }

  public void setAllowedMethods(Set<String> allowedMethods) {
    this.allowedMethods = allowedMethods;
  }

  interface ResponseStrategy {
    Response send(ServerInvocationContext context, Object entity) throws IOException;
  }

  class VoidResponseStrategy implements ResponseStrategy {

    @Override
    public Response send(ServerInvocationContext context, Object entity) throws IOException {
      context.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
      return writeTo(context, null, null, null, null);
    }

  }

  class GenericEntityResponseStrategy implements ResponseStrategy {

    @Override
    public Response send(ServerInvocationContext context, Object entity) throws IOException {
      GenericEntity<?> genericEntity = (GenericEntity<?>) entity;
      if (genericEntity.getEntity() == null) {
        context.setStatus(HttpURLConnection.HTTP_NO_CONTENT);
        return writeTo(context, null, null, null, null);
      } else {
        return writeTo(context, genericEntity.getEntity(), genericEntity.getRawType(),
            genericEntity.getType(), null);
      }
    }

  }

  class DefaultResponseStrategy implements ResponseStrategy {

    @Override
    public Response send(ServerInvocationContext context, Object entity) throws IOException {
      return writeTo(context, entity, type, genericType, null);
    }

  }

  class ResponseResponseStrategy implements ResponseStrategy {

    @Override
    public Response send(ServerInvocationContext context, Object entity) throws IOException {
      MediaType responseMediaType = context.getProduce();
      Response resp = (Response) entity;
      if (resp.getMediaType() != null) {
        responseMediaType = resp.getMediaType();
      }
      MultivaluedMap<String, String> headers = resp.getStringHeaders();
      if (headers.containsKey(HttpHeader.CONTENT_TYPE)) {
        responseMediaType = FastMediaType.valueOf(headers.getFirst(HttpHeaders.CONTENT_TYPE));
      }
      context.setProduce(FastMediaType.valueOf(responseMediaType));
      Class<?> entityClass = resp.getEntity() == null ? null : resp.getEntity().getClass();
      context.setStatus(resp.getStatus());
      return writeTo(context, resp.getEntity(), entityClass, entityClass, headers);
    }

  }

  @Override
  public AbstractResource addFilter(ContainerResponseFilter rf) {
    if (responseFilters == null) {
      responseFilters = new ArrayList<>();
    }
    responseFilters.add(rf);
    Collections.sort(responseFilters, Filters.SORT);
    return this;
  }

  @Override
  public AbstractResource select(ServerInvocationContext context) {
    if (!isvoid) {
      FastMediaType responseMediaType = findResponseMediatype(context);

      if (responseMediaType == null) {
        return null;
      }

      context.setProduce(responseMediaType);
    }
    if (consume != null) {
      FastMediaType consumeMediaType = findConsumeMediatype(context);

      if (consumeMediaType == null) {
        context.setProduce(null);
        return null;
      }

      if (postParam != null) {
        context.setReader(readers[consumeMediaType.getId()]);
      }
      context.setConsume(consumeMediaType);
    }
    return this;
  }

}
