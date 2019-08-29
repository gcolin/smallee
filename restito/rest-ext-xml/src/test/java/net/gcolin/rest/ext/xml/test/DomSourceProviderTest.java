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

import net.gcolin.rest.ext.xml.DomSourceProvider;

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
import javax.xml.transform.dom.DOMSource;

/**
 * @author Gaël COLIN
 * @since 1.0
 */
public class DomSourceProviderTest extends SourceProviderTest<DOMSource, DomSourceProvider> {

  @Override
  DomSourceProvider getProvider() {
    return new DomSourceProvider();
  }

  @Test
  public void writeToTest() throws IOException, SAXException, ParserConfigurationException {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder buildler = dbFactory.newDocumentBuilder();
    Document doc = buildler.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    doc.setXmlStandalone(true);
    writeToTest0(new DOMSource(doc));
  }

  @Test
  public void readFromTest() throws IOException {
    ByteArrayInputStream bout = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    DOMSource source = new DomSourceProvider().readFrom(DOMSource.class, DOMSource.class,
        new Annotation[0], MediaType.APPLICATION_XML_TYPE, null, bout);
    Assert.assertEquals("hello", source.getNode().getFirstChild().getNodeName());
    Assert.assertEquals("world", source.getNode().getFirstChild().getTextContent());
  }
}
