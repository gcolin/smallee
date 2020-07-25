package net.gcolin.rest.multipart;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public class BodyPart {

	private final MediaType mediaType;
	private final Object entity;
	private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

	public BodyPart(MediaType mediaType, Object entity) {
		this.mediaType = mediaType;
		this.entity = entity;
	}

	public MultivaluedMap<String, String> getHeaders() {
		return this.headers;
	}

	public MediaType getMediaType() {
		return this.mediaType;
	}

	public Object getEntity() {
		return entity;
	}
}
