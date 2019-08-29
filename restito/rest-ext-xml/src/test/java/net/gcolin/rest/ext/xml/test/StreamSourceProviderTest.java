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

package net.gcolin.rest.ext.xml.test;

import net.gcolin.rest.ext.xml.StreamSourceProvider;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class StreamSourceProviderTest
    extends SourceProviderTest<StreamSource, StreamSourceProvider> {

  @Override
  StreamSourceProvider getProvider() {
    return new StreamSourceProvider();
  }

  @Test
  public void writeToTest()
      throws IOException, SAXException, ParserConfigurationException, XMLStreamException {
    writeToTest0(new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
  }

  @Test
  public void readFromTest()
      throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
    ByteArrayInputStream bout = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    StreamSource source = new StreamSourceProvider().readFrom(StreamSource.class,
        StreamSource.class, new Annotation[0], MediaType.APPLICATION_XML_TYPE, null, bout);
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = dbFactory.newDocumentBuilder();
    Document doc = builder.parse(source.getInputStream());
    Assert.assertEquals("hello", doc.getFirstChild().getNodeName());
    Assert.assertEquals("world", doc.getFirstChild().getTextContent());
  }
}
