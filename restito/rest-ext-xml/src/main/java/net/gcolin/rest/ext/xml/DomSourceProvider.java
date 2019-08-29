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

package net.gcolin.rest.ext.xml;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

/**
 * Read/Write DOMSource entity.
 * 
 * @author Gaël COLIN
 * @since 1.0
 * @see DOMSource
 */
@Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.WILDCARD})
@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
public class DomSourceProvider extends SourceProvider<DOMSource> {

  private DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();

  public DomSourceProvider() {
    super(DOMSource.class);
  }

  @Override
  public DOMSource readFrom(Class<DOMSource> type, Type genericType, Annotation[] annotations,
      MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
          throws IOException {
    try {
      return new DOMSource(df.newDocumentBuilder().parse(entityStream));
    } catch (SAXException | ParserConfigurationException ex) {
      throw new IOException(ex);
    }
  }

}
