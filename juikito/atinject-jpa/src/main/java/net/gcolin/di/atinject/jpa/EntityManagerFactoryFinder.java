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
package net.gcolin.di.atinject.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.gcolin.di.core.InjectException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provide an EntityManager Proxy.
 *
 * @author Gaël COLIN
 * @since 1.0
 */
public abstract class EntityManagerFactoryFinder {

    private EntityManagerFactory emf;
    private final JpaExtension extension;
    private final ClassLoader classLoader;

    public EntityManagerFactoryFinder(JpaExtension extension,
            ClassLoader classLoader) {
        this.extension = extension;
        this.classLoader = classLoader;
    }

    protected abstract String key();

    protected abstract String unitName();

    protected abstract Map<String, Object> properties();

    protected synchronized EntityManagerFactory getEmf() {
        if (emf == null) {
            String key = key();
            emf = extension.getEmfs().get(key);
            if (emf == null) {
                String unitName = unitName();
                String name = unitName.length() == 0
                        ? getDefaultUnitPersistenceName(extension, classLoader)
                        : unitName;
                emf = Persistence.createEntityManagerFactory(name, properties());
                extension.getEmfs().put(key, emf);
            }
        }
        return emf;
    }

    private String getDefaultUnitPersistenceName(JpaExtension extension, ClassLoader classLoader) {
        if (extension.getDefaultUnitPersistenceName() != null) {
            return extension.getDefaultUnitPersistenceName();
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = dbFactory.newDocumentBuilder();

            try (InputStream in = classLoader.getResourceAsStream("META-INF/persistence.xml")) {
                Document doc = builder.parse(in);
                NodeList pu = doc.getElementsByTagName("persistence-unit");
                if (pu.getLength() > 0) {
                    extension.setDefaultUnitPersistenceName(
                            pu.item(0).getAttributes().getNamedItem("name").getNodeValue());
                    return extension.getDefaultUnitPersistenceName();
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            throw new InjectException(ex);
        }
        return null;
    }

}
