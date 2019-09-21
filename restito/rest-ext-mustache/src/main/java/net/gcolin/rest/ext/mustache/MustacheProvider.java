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

package net.gcolin.rest.ext.mustache;

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

import net.gcolin.common.Priority;
import net.gcolin.common.io.Io;
import net.gcolin.mustache.MustacheContext;

/**
 * Write MustacheView.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@ConstrainedTo(RuntimeType.SERVER)
@Produces({ MediaType.WILDCARD })
@Priority(10)
public class MustacheProvider implements MessageBodyWriter<MustacheView> {

	private MustacheContext context;
	private String charset;

	public MustacheProvider(MustacheContext context) {
		this(context, "utf-8");
	}

	public MustacheProvider(MustacheContext context, String charset) {
		this.context = context;
		this.charset = charset;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type == MustacheView.class;
	}

	@Override
	public long getSize(MustacheView entity, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(MustacheView entity, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
			throws IOException {
		try (Writer writer = Io.writer(entityStream, charset)) {
			context.getTemplate(entity.getPath()).render(entity.getModel(), writer);
		}
	}

}
