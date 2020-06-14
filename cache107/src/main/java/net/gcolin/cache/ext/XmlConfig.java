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

package net.gcolin.cache.ext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.gcolin.cache.CacheImpl;
import net.gcolin.cache.CachingProviderImpl;

/**
 * An helper to read XML configuration file.
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 */
public class XmlConfig {

	private XmlConfig() {
	}

	/**
	 * Read an XML configuration. The cache manager URI is the path to the XML
	 * configuration file.
	 * 
	 * @param manager the cache manager
	 */
	@SuppressWarnings("unchecked")
	public static void config(CacheManager manager) {
		ClassLoader classLoader = manager.getClassLoader();
		try {
			Enumeration<URL> all = classLoader.getResources(manager.getURI().toString());
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			while (all.hasMoreElements()) {
				try (InputStream in = all.nextElement().openStream()) {
					Document doc = builder.parse(in);
					NodeList cacheList = doc.getElementsByTagName("cache");
					for (int i = 0; i < cacheList.getLength(); i++) {
						Node node = cacheList.item(i);
						NodeList children = node.getChildNodes();
						String name = null;
						int maxSizeMemory = -1; // no limit
						int maxSizeDisk = -2; // no store
						boolean statistics = false;
						boolean management = false;
						long expiryCreate = Long.MAX_VALUE;
						long expiryAccess = Long.MAX_VALUE;
						long expiryUpdate = Long.MAX_VALUE;
						long expiryIdle = Long.MAX_VALUE;
						boolean byValue = false;
						boolean persistent = false;
						String keyType = null;
						String valueType = null;
						String dir = null;
						String fileName = null;

						for (int j = 0; j < children.getLength(); j++) {
							Node nchild = children.item(j);
							switch (nchild.getNodeName()) {
							case "name":
								name = nchild.getTextContent().trim();
								break;
							case "maxSizeMemory":
								maxSizeMemory = Integer.parseInt(nchild.getTextContent().trim());
								break;
							case "maxSizeDisk":
								maxSizeDisk = Integer.parseInt(nchild.getTextContent().trim());
								break;
							case "management":
								management = Boolean.parseBoolean(nchild.getTextContent().trim());
								break;
							case "statistics":
								statistics = Boolean.parseBoolean(nchild.getTextContent().trim());
								break;
							case "expiryCreate":
								expiryCreate = Long.parseLong(nchild.getTextContent().trim());
								break;
							case "expiryAccess":
								expiryAccess = Long.parseLong(nchild.getTextContent().trim());
								break;
							case "expiryUpdate":
								expiryUpdate = Long.parseLong(nchild.getTextContent().trim());
								break;
							case "expiryIdle":
								expiryIdle = Long.parseLong(nchild.getTextContent().trim());
								break;
							case "byValue":
								byValue = Boolean.parseBoolean(nchild.getTextContent().trim());
								break;
							case "persistent":
								persistent = Boolean.parseBoolean(nchild.getTextContent().trim());
								break;
							case "keyType":
								keyType = nchild.getTextContent().trim();
								break;
							case "valueType":
								valueType = nchild.getTextContent().trim();
								break;
							case "dir":
								dir = nchild.getTextContent().trim();
								break;
							case "fileName":
								fileName = nchild.getTextContent().trim();
								break;
							default:
								break;
							}
						}

						MutableConfiguration<Object, Object> config = new MutableConfiguration<>();
						if (keyType != null && valueType != null) {
							config.setTypes((Class<Object>) classLoader.loadClass(keyType),
									(Class<Object>) classLoader.loadClass(valueType));
						}

						Duration expiryCreateDuration = toDuration(expiryCreate);
						Duration expiryAccessDuration = toDuration(expiryAccess);
						Duration expiryUpdateDuration = toDuration(expiryUpdate);
						ExpiryPolicy expiryPolicy = new ExpiryPolicy() {

							@Override
							public Duration getExpiryForUpdate() {
								return expiryUpdateDuration;
							}

							@Override
							public Duration getExpiryForCreation() {
								return expiryCreateDuration;
							}

							@Override
							public Duration getExpiryForAccess() {
								return expiryAccessDuration;
							}
						};
						config.setExpiryPolicyFactory(new FactoryBuilder.SingletonFactory<>(expiryPolicy));
						config.setManagementEnabled(management);
						config.setStatisticsEnabled(statistics);
						config.setStoreByValue(byValue);
						if (maxSizeDisk != -2) {
							File dirFile = new File(dir == null ? System.getProperty("java.io.tmpdir") : dir);
							String dirName = fileName;
							if (dirName == null) {
								dirName = name;
							}
							@SuppressWarnings("resource")
							CacheFile<Object, Object> file = new CacheFile<>(dirName, dirFile, !persistent,
									config.getKeyType(), config.getValueType());
							file.setMaxSize(maxSizeDisk);
							if (expiryIdle != Long.MAX_VALUE) {
								file.setExpiry(expiryIdle);
							}
							file.asIdle(config);
						}
						Cache<Object, Object> cache = manager.createCache(name, config);
						if (maxSizeMemory > 0) {
							cache.unwrap(CacheImpl.class).setMaxSize(maxSizeMemory);
						}
					}
				}
			}
		} catch (SAXException | ClassNotFoundException | ParserConfigurationException | IOException e1) {
			CachingProviderImpl.LOGGER.error("cannot load cache config file " + manager.getURI(), e1);
		}
	}

	private static Duration toDuration(long duration) {
		if (duration == Long.MAX_VALUE) {
			return Duration.ETERNAL;
		} else if (duration == 0) {
			return Duration.ZERO;
		} else {
			return new Duration(TimeUnit.MILLISECONDS, duration);
		}
	}

}
