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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import net.gcolin.common.lang.Strings;

/**
 * Read/Write MultivaluedMap entity from Http payload.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@Produces({ MediaType.APPLICATION_FORM_URLENCODED })
@Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
public class FormProvider extends Provider<MultivaluedMap<String, String>> {

	public FormProvider() {
		super(MultivaluedMap.class);
	}

	@Override
	public void writeTo(MultivaluedMap<String, String> map, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException {
		try (Writer writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
			fillFormMap(map, writer);
		}
	}

	private void fillFormMap(MultivaluedMap<String, String> map, Writer writer) throws IOException {
		boolean fst = true;
		for (Entry<String, List<String>> e : map.entrySet()) {
			for (String s : e.getValue()) {
				if (fst) {
					fst = false;
				} else {
					writer.append('&');
				}
				writer.write(e.getKey());
				writer.append('=');
				writer.write(s);
			}
		}
	}

	@Override
	public MultivaluedMap<String, String> readFrom(Class<MultivaluedMap<String, String>> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
			InputStream entityStream) throws IOException {
		MultivaluedMap<String, String> map = new MultivaluedHashMap<String, String>();
		int ch;
		String key = null;
		StringBuilder str = new StringBuilder();
		while ((ch = entityStream.read()) != -1) {
			if (ch == '=') {
				key = str.toString();
				str.setLength(0);
			} else if (ch == '&') {
				map.add(Strings.decodeUrl(key), Strings.decodeUrl(str.toString()));
				str.setLength(0);
			} else {
				str.append((char) ch);
			}
		}
		map.add(Strings.decodeUrl(key), Strings.decodeUrl(str.toString()));
		return map;
	}

}
