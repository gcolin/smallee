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

package net.gcolin.rest.ext.freemarker;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Produces;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.gcolin.common.io.Io;

/**
 * Write JspView.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@ConstrainedTo(RuntimeType.SERVER)
@Produces({ MediaType.WILDCARD })
public class FreemarkerProvider implements MessageBodyWriter<FreemarkerView> {

	private Configuration configuration;
	private String charset;

	public FreemarkerProvider(Configuration configuration) {
		this(configuration, "UTF-8");
	}

	public FreemarkerProvider(Configuration configuration, String charset) {
		this.configuration = configuration;
		this.charset = charset;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type == FreemarkerView.class;
	}

	@Override
	public long getSize(FreemarkerView entity, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(FreemarkerView entity, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
			throws IOException {
		Template tmpl = configuration.getTemplate(entity.getPath());

		try (Writer writer = Io.writer(entityStream, charset)) {
			tmpl.process(entity.getModel(), writer);
		} catch (TemplateException ex) {
			throw new IOException(ex);
		}
	}

}
