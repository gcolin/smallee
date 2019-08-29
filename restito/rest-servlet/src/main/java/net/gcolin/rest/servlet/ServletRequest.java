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

package net.gcolin.rest.servlet;

import net.gcolin.common.collection.Collections2;
import net.gcolin.common.collection.Func;
import net.gcolin.common.lang.Header;
import net.gcolin.common.lang.Headers;
import net.gcolin.common.lang.Strings;
import net.gcolin.rest.FastMediaType;
import net.gcolin.rest.util.HttpHeader;
import net.gcolin.rest.util.lb.DateHeaderParamConverter;
import net.gcolin.rest.util.lb.DateParamConverter;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

/**
 * Request from an HttpServletRequest.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class ServletRequest implements Request {

	private static final int SEC = 1000;
	private HttpServletRequest request;

	public ServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public ResponseBuilder evaluatePreconditions() {
		return Response.status(Status.PRECONDITION_FAILED);
	}

	@Override
	public ResponseBuilder evaluatePreconditions(EntityTag arg0) {
		return Response.status(Status.PRECONDITION_FAILED);
	}

	@Override
	public ResponseBuilder evaluatePreconditions(Date lastModified) {
		final long lastModifiedTime = lastModified.getTime();
		String ifUnmodifiedSinceHeader = request.getHeader(HttpHeader.IF_UNMODIFIED_SINCE);
		if (!Strings.isNullOrEmpty(ifUnmodifiedSinceHeader)) {
			Date date = new DateHeaderParamConverter().fromString(ifUnmodifiedSinceHeader);
			if(date == null) {
				date = new DateParamConverter().fromString(ifUnmodifiedSinceHeader);
			}
			if (date != null && roundDown(lastModifiedTime) > date.getTime()) {
				// 412 Precondition Failed
				return Response.status(Status.PRECONDITION_FAILED);
			}
		}
		String ifModifiedSinceHeader = request.getHeader(HttpHeader.IF_MODIFIED_SINCE);

		if (!Strings.isNullOrEmpty(ifModifiedSinceHeader) && isNotModified(ifModifiedSinceHeader, lastModifiedTime)) {
			return Response.status(Status.NOT_MODIFIED);
		}

		return null;
	}

	@Override
	public ResponseBuilder evaluatePreconditions(Date lastModified, EntityTag etag) {
		return evaluatePreconditions(lastModified);
	}

	private boolean isNotModified(String ifModifiedSinceHeader, long lastModifiedTime) {
		final String httpMethod = getMethod();
		if (HttpMethod.GET.equals(httpMethod) || HttpMethod.HEAD.equals(httpMethod)) {
			Date date = new DateHeaderParamConverter().fromString(ifModifiedSinceHeader);
			if(date == null) {
				date = new DateParamConverter().fromString(ifModifiedSinceHeader);
			}
			if (date != null && roundDown(lastModifiedTime) <= date.getTime()) {
				// 304 Not modified
				return true;
			}
		}
		return false;
	}

	/**
	 * Round down the time to the nearest second.
	 * 
	 * @param time
	 *            the time to round down.
	 * @return the rounded down time.
	 */
	private long roundDown(long time) {
		return time - time % SEC;
	}

	@Override
	public String getMethod() {
		return request.getMethod();
	}

	@Override
	public Variant selectVariant(List<Variant> variants) {
		List<FastMediaType> acceptMediaTypes = Collections2
				.toList(FastMediaType.iterator(request.getHeader(HttpHeader.ACCEPT)));
		List<Locale> locales = Collections2.toList(request.getLocales());
		String encodingHeader = request.getHeader(HttpHeader.ACCEPT_ENCODING);
		List<String> acceptEncoding = encodingHeader == null ? Collections.emptyList() : Func.map(Headers.parse(encodingHeader),
				Header::getValue);
		for (Variant v : variants) {
			boolean encoding = v.getEncoding() == null || acceptEncoding.contains(v.getEncoding());
			boolean language = v.getLanguage() == null || locales.contains(v.getLanguage());
			if (encoding && language) {
				if (v.getMediaType() != null) {
					FastMediaType fm = FastMediaType.valueOf(v.getMediaType());
					for (FastMediaType mediaType : acceptMediaTypes) {
						if (mediaType.isCompatible(fm)) {
							return v;
						}
					}
				} else {
					return v;
				}
			}
		}
		return null;
	}

}
