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

package net.gcolin.rest.ext.jaxb;

import net.gcolin.common.reflect.Reflect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Read/Write Collection form XML.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
public class JaxbCollectionProvider
    implements
      MessageBodyWriter<Object>,
      MessageBodyReader<Object> {

  private static final String DEFAULT = "##default";
  private Map<Class<?>, String> names = new HashMap<>();
  private Map<String, Context> charsets = new HashMap<>();
  private XMLInputFactory xf = XMLInputFactory.newInstance();
  private static final Class<?>[] DEFAULT_IMPLS =
      new Class[] {ArrayList.class, LinkedList.class, HashSet.class, TreeSet.class, Stack.class};

  @javax.ws.rs.core.Context
  private ContextResolver<JAXBContext> contextResolver;

  public JaxbCollectionProvider() {}

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return checkType(type) && checkMediaType(mediaType);
  }

  private boolean checkMediaType(MediaType mediaType) {
    return mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)
        || mediaType.isCompatible(MediaType.TEXT_XML_TYPE);
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return checkType(type) && checkMediaType(mediaType);
  }

  private boolean checkType(Class<?> type) {
    return Collection.class.isAssignableFrom(type) || type.isArray();
  }

  @Override
  public long getSize(Object entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return -1;
  }

  private Context getContext(MediaType mediaType) {
    String charset = mediaType.getParameters().get("charset");
    Context charsetContext = charsets.get(charset);
    if (charsetContext == null) {
      charsetContext =
          new Context(charset == null ? StandardCharsets.UTF_8 : Charset.forName(charset));
      charsets.put(charset, charsetContext);
    }
    return charsetContext;
  }

  @Override
  public void writeTo(Object entity, Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
          throws IOException {
    if (entity == null) {
      throw new NotFoundException();
    }
    try {
      Context charsetContext = getContext(mediaType);

      entityStream.write(charsetContext.h1);
      entityStream.write(charsetContext.cs);
      entityStream.write(charsetContext.h2);

      Class<?> rtype = getElementClass(type, genericType);
      if (rtype == null) {
        if (type.isArray()) {
          rtype = type.getComponentType();
        } else if (!((Collection<?>) entity).isEmpty()) {
          rtype = ((Collection<?>) entity).iterator().next().getClass();
        } else {
          rtype = Undefined.class;
        }
      }

      String name = getRootName(rtype);
      entityStream.write(charsetContext.se);
      entityStream.write(name.getBytes(charsetContext.charset));
      entityStream.write(charsetContext.eb);

      JAXBContext ctx = contextResolver.getContext(rtype);
      Marshaller marshaller = ctx.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

      Collection<?> cl = type.isArray() ? Arrays.asList((Object[]) entity) : (Collection<?>) entity;
      for (Object o : cl) {
        marshaller.marshal(o, entityStream);
      }

      entityStream.write(charsetContext.ee);
      entityStream.write(name.getBytes(charsetContext.charset));
      entityStream.write(charsetContext.eb);
    } catch (JAXBException ex) {
      throw new IOException(ex);
    }
  }

  private String getRootName(Class<?> rtype) {
    String name = names.get(rtype);
    if (name == null) {
      XmlRootElement xr = rtype.getAnnotation(XmlRootElement.class);
      if (xr == null) {
        return rtype.getSimpleName();
      }
      if (!DEFAULT.equals(xr.name())) {
        name = xr.name();
      }
      if (name == null) {
        name = rtype.getSimpleName();
        name = name.substring(0, 1).toLowerCase() + name.substring(1);
      }
      names.put(rtype, name);
    }
    return name;
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException {
    try {
      final Class<?> rtype = getElementClass(type, genericType);
      final Unmarshaller u = contextResolver.getContext(rtype).createUnmarshaller();
      final XMLStreamReader r =
          xf.createXMLStreamReader(entityStream, getContext(mediaType).charset.name());
      boolean jaxbElement = false;

      Collection<Object> collection = createCollection(type);

      int event = position(r);

      while (event != XMLStreamReader.END_DOCUMENT) {
        if (rtype.isAnnotationPresent(XmlRootElement.class)) {
          collection.add(u.unmarshal(r));
        } else if (rtype.isAnnotationPresent(XmlType.class)) {
          collection.add(u.unmarshal(r, rtype).getValue());
        } else {
          collection.add(u.unmarshal(r, rtype));
          jaxbElement = true;
        }

        // Move to next peer (if any)
        event = nextPeer(r);
      }

      return type.isArray()
          ? createArray(collection, jaxbElement ? JAXBElement.class : rtype)
          : collection;
    } catch (XMLStreamException | JAXBException ex) {
      throw new IOException(ex);
    }
  }

  private int nextPeer(final XMLStreamReader reader) throws XMLStreamException {
    int event = reader.getEventType();
    while (event != XMLStreamReader.START_ELEMENT && event != XMLStreamReader.END_DOCUMENT) {
      event = reader.next();
    }
    return event;
  }

  @SuppressWarnings("unchecked")
  private Collection<Object> createCollection(Class<Object> type) {
    Collection<Object> collection = null;
    if (type.isArray()) {
      collection = new ArrayList<Object>();
    } else {
      if (Modifier.isAbstract(type.getModifiers())) {
        collection = createDefaultCollection(type);
      } else {
        collection = (Collection<Object>) Reflect.newInstance(type);
      }
    }
    if (collection == null) {
      throw new InternalServerErrorException("cannot create collection");
    }
    return collection;
  }

  @SuppressWarnings("unchecked")
  private Collection<Object> createDefaultCollection(Class<Object> type) {
    for (Class<?> c : DEFAULT_IMPLS) {
      if (type.isAssignableFrom(c)) {
        return (Collection<Object>) Reflect.newInstance(c);
      }
    }
    return null;
  }

  private int position(final XMLStreamReader reader) throws XMLStreamException {
    // Move to root element
    int event = reader.next();
    while (event != XMLStreamReader.START_ELEMENT) {
      event = reader.next();
    }

    // Move to first child (if any)
    event = reader.next();
    while (event != XMLStreamReader.START_ELEMENT && event != XMLStreamReader.END_DOCUMENT) {
      event = reader.next();
    }

    return event;
  }

  private static Object createArray(Collection<?> collection, Class<?> componentType) {
    Object array = Array.newInstance(componentType, collection.size());
    int idx = 0;
    for (Object value : collection) {
      Array.set(array, idx++, value);
    }
    return array;
  }

  private Class<?> getElementClass(Class<?> type, Type genericType) {
    Type ta;
    if (genericType instanceof ParameterizedType) {
      // List case
      ta = ((ParameterizedType) genericType).getActualTypeArguments()[0];
    } else if (genericType instanceof GenericArrayType) {
      // GenericArray case
      ta = ((GenericArrayType) genericType).getGenericComponentType();
    } else {
      // Array case
      ta = type.getComponentType();
    }
    if (ta instanceof ParameterizedType) {
      // JAXBElement case
      ta = ((ParameterizedType) ta).getActualTypeArguments()[0];
    }
    return (Class<?>) ta;
  }

  private static class Context {
    byte[] h1;
    byte[] h2;
    byte[] cs;
    byte[] se;
    byte[] ee;
    byte[] eb;
    Charset charset;

    public Context(Charset charset) {
      this.charset = charset;
      h1 = "<?xml version=\"1.0\" encoding=\"".getBytes(charset);
      h2 = "\" standalone=\"yes\"?>".getBytes(charset);
      cs = charset.name().getBytes(charset);
      se = "<".getBytes(charset);
      ee = "</".getBytes(charset);
      eb = "s>".getBytes(charset);
    }
  }

}
