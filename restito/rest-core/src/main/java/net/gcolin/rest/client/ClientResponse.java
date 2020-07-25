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

package net.gcolin.rest.client;

import net.gcolin.common.io.ByteArrayInputStream;
import net.gcolin.common.io.ByteArrayOutputStream;
import net.gcolin.common.io.Io;
import net.gcolin.rest.AbstractResponse;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.InvocationContext;
import net.gcolin.rest.MessageBodyReaderDecorator;
import net.gcolin.rest.util.HeaderObjectMap;
import net.gcolin.rest.util.HeaderPair;
import net.gcolin.rest.util.HttpHeader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.ReaderInterceptor;

/**
 * The Response implementation for REST client.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ClientResponse extends AbstractResponse {

	private URLConnection conn;
	private Object entity;
	private ByteArrayInputStream buffer;
	private MultivaluedMap<String, String> stringheaders;
	private MultivaluedMap<String, Object> headers;
	private ClientFeatureBuilder builder;
	private int status;
	private ClientResponseContext context;
	private InputStream input;

	/**
	 * Create a ClientResponse
	 * 
	 * @param conn a connection
	 * @param builder features helper
	 * @throws IOException if an I/O error occurs.
	 */
	public ClientResponse(URLConnection conn, ClientFeatureBuilder builder) throws IOException {
		this.conn = conn;
		this.builder = builder;
		if (conn instanceof HttpURLConnection) {
			status = ((HttpURLConnection) conn).getResponseCode();
		}
		extractHeaders();
	}

	/**
	 * Get the InputStream.
	 * 
	 * @return the connection InputStream
	 */
	public InputStream getInput() {
		if (input == null) {
			try {
				input = hasHttpError() ? ((HttpURLConnection) conn).getErrorStream() : conn.getInputStream();
			} catch (IOException ex) {
				throw new ResponseProcessingException(this, "cannot get input", ex);
			}
		}
		return input;
	}

	public boolean hasHttpError() {
		return status >= 400 && conn instanceof HttpURLConnection;
	}

	/**
	 * Get the context.
	 * 
	 * @return the response context
	 */
	public ClientResponseContext getContext() {
		if (context == null) {
			context = new Context();
		}
		return context;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public Object getEntity() {
		if (entity != null) {
			return entity;
		}
		if (buffer != null) {
			return buffer;
		}
		return getInput();
	}

	@Override
	public <T> T readEntity(Class<T> entityType, Annotation[] annotations) {
		return read0(entityType, entityType, annotations);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T readEntity(GenericType<T> entityType, Annotation[] annotations) {
		return read0((Class<T>) entityType.getRawType(), entityType.getType(), annotations);
	}

	@SuppressWarnings("unchecked")
	private <T> T read0(Class<T> entityType, Type genericType, Annotation[] annotations) {
		if(entityType == InputStream.class) {
			return (T) (buffer == null ? getInput() : buffer);
		}
		InputStream in = null;
		try {
			in = buffer == null ? getInput() : buffer;
			T obj;
			if (builder.getReaderInterceptors().isEmpty()) {
				MediaType type = getMediaType();
				if (type == null) {
					return null;
				}
				obj = (T) builder.getProviders().readFrom((Class<Object>) entityType, genericType, annotations, type,
						getStringHeaders(), in);
			} else {
				InvocationContext ctx = new InvocationContext();
				ctx.setConsume(FastMediaType.valueOf(getMediaType()));
				ctx.setReader(builder.getProviders());
				MessageBodyReaderDecorator md = new MessageBodyReaderDecorator();
				md.add(builder.getReaderInterceptors()
						.toArray(new ReaderInterceptor[builder.getReaderInterceptors().size()]));
				obj = (T) md.readFrom(ctx, annotations, entityType, genericType, getStringHeaders());
			}
			entity = obj;
			return obj;
		} catch (IOException ex) {
			throw new ResponseProcessingException(this, "cannot read entity", ex);
		} finally {
			Io.close(in);
			close();
		}
	}

	@Override
	public boolean hasEntity() {
		return getLength() != 0;
	}

	@Override
	public boolean bufferEntity() {
		if (buffer == null) {
			InputStream in = null;
			try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
				in = conn.getInputStream();
				Io.copy(in, bout);
				buffer = new ByteArrayInputStream(bout.toByteArray());
				bout.release();
			} catch (IOException ex) {
				throw new ResponseProcessingException(this, "cannot buffer entity", ex);
			} finally {
				Io.close(in);
				close();
			}
			
			return true;
		}
		return false;
	}

	@Override
	public void close() {
		Io.close(conn);
	}

	@Override
	public MultivaluedMap<String, String> getStringHeaders() {
		return stringheaders;
	}

	@Override
	public MultivaluedMap<String, Object> getMetadata() {
		return headers;
	}

	private void extractHeaders() {
		HeaderPair pair = HeaderObjectMap.createHeaders();

		MultivaluedMap<String, String> sheaders = pair.getValue();
		MultivaluedMap<String, Object> ho = pair.getKey();

		for (Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
			if (header.getKey() == null) {
				continue;
			}

			String lower = header.getKey().toLowerCase();
			Function<String, List<Object>> converter = HttpHeader.CONVERTERS.get(lower);
			for (String value : header.getValue()) {
				if (converter == null) {
					ho.add(lower, value);
				} else {
					for (Object o : converter.apply(value)) {
						ho.add(lower, o);
					}
				}
			}
		}

		headers = ho;
		stringheaders = sheaders;
	}

	private class Context implements ClientResponseContext {

		@Override
		public int getStatus() {
			return status;
		}

		@Override
		public void setStatus(int code) {
			status = code;
		}

		@Override
		public StatusType getStatusInfo() {
			return Status.fromStatusCode(status);
		}

		@Override
		public void setStatusInfo(StatusType statusInfo) {
			status = statusInfo.getStatusCode();
		}

		@Override
		public MultivaluedMap<String, String> getHeaders() {
			return ClientResponse.this.getStringHeaders();
		}

		@Override
		public String getHeaderString(String name) {
			return ClientResponse.this.getStringHeaders().getFirst(name);
		}

		@Override
		public Set<String> getAllowedMethods() {
			return ClientResponse.this.getAllowedMethods();
		}

		@Override
		public Date getDate() {
			return ClientResponse.this.getDate();
		}

		@Override
		public Locale getLanguage() {
			return ClientResponse.this.getLanguage();
		}

		@Override
		public int getLength() {
			return ClientResponse.this.getLength();
		}

		@Override
		public MediaType getMediaType() {
			return ClientResponse.this.getMediaType();
		}

		@Override
		public Map<String, NewCookie> getCookies() {
			return ClientResponse.this.getCookies();
		}

		@Override
		public EntityTag getEntityTag() {
			return ClientResponse.this.getEntityTag();
		}

		@Override
		public Date getLastModified() {
			return ClientResponse.this.getLastModified();
		}

		@Override
		public URI getLocation() {
			return ClientResponse.this.getLocation();
		}

		@Override
		public Set<Link> getLinks() {
			return ClientResponse.this.getLinks();
		}

		@Override
		public boolean hasLink(String relation) {
			return ClientResponse.this.hasLink(relation);
		}

		@Override
		public Link getLink(String relation) {
			return ClientResponse.this.getLink(relation);
		}

		@Override
		public Builder getLinkBuilder(String relation) {
			return ClientResponse.this.getLinkBuilder(relation);
		}

		@Override
		public boolean hasEntity() {
			return ClientResponse.this.hasEntity();
		}

		@Override
		public InputStream getEntityStream() {
			return getInput();
		}

		@Override
		public void setEntityStream(InputStream input) {
			ClientResponse.this.input = input;
		}

	}

}
