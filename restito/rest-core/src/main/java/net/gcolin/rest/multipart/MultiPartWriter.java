package net.gcolin.rest.multipart;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;


@Produces({MediaType.MULTIPART_FORM_DATA, MediaType.WILDCARD})
public class MultiPartWriter implements MessageBodyWriter<MultiPart> {
	
	private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

	@Context Providers providers;
	
	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type == MultiPart.class;
	}

	@Override
	public long getSize(MultiPart t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void writeTo(MultiPart entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		// Verify that there is at least one body part
        if (entity.getParts().isEmpty()) {
            throw new WebApplicationException(new IllegalArgumentException("Must specify at least one body part"));
        }

        httpHeaders.putSingle("MIME-Version", "1.0");
        // Initialize local variables we need
        final Writer writer = new BufferedWriter(new OutputStreamWriter(entityStream, StandardCharsets.UTF_8));

        // Determine the boundary string to be used, creating one if needed
        final MediaType boundaryMediaType = Boundary.addBoundary(mediaType);
        if (boundaryMediaType != mediaType) {
        	httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, boundaryMediaType);
        }

        final String boundaryString = boundaryMediaType.getParameters().get("boundary");
        // Iterate through the body parts for this message
        boolean isFirst = true;
        for (final BodyPart bodyPart : entity.getParts()) {

            // Write the leading boundary string
            if (isFirst) {
                isFirst = false;
                writer.write("--");
            } else {
                writer.write("\r\n--");
            }
            writer.write(boundaryString);
            writer.write("\r\n");

            // Write the headers for this body part
            final MediaType bodyMediaType = bodyPart.getMediaType();
            if (bodyMediaType == null) {
                throw new WebApplicationException(new IllegalArgumentException("Missing body part media type"));
            }
            final MultivaluedMap<String, String> bodyHeaders = bodyPart.getHeaders();
            bodyHeaders.putSingle("Content-Type", bodyMediaType.toString());

            // Iterate for the nested body parts
            for (final Map.Entry<String, List<String>> entry : bodyHeaders.entrySet()) {
                // Write this header and its value(s)
                writer.write(entry.getKey());
                writer.write(':');
                boolean first = true;
                for (String value : entry.getValue()) {
                    if (first) {
                        writer.write(' ');
                        first = false;
                    } else {
                        writer.write(',');
                    }
                    writer.write(value);
                }
                writer.write("\r\n");
            }

            // Mark the end of the headers for this body part
            writer.write("\r\n");
            writer.flush();

            // Write the entity for this body part
            Object bodyEntity = bodyPart.getEntity();
            if (bodyEntity == null) {
                throw new WebApplicationException(
                        new IllegalArgumentException("Missing body part entity of type '" + bodyMediaType + "'"));
            }
            
            final MessageBodyWriter bodyWriter = providers.getMessageBodyWriter(
            		bodyEntity.getClass(),
            		bodyEntity.getClass(),
                    EMPTY_ANNOTATIONS,
                    bodyMediaType);

            if (bodyWriter == null) {
                throw new WebApplicationException(
                        new IllegalArgumentException(
                                "No MessageBodyWriter for body part of type '" +
                                        bodyEntity.getClass().getName() + "' and media type '" +
                                        bodyMediaType + "'"));
            }

            bodyWriter.writeTo(
                    bodyEntity,
                    bodyEntity.getClass(),
                    bodyEntity.getClass(),
                    EMPTY_ANNOTATIONS,
                    bodyMediaType,
                    bodyHeaders,
                    entityStream);
        }

        // Write the final boundary string
        writer.write("\r\n--");
        writer.write(boundaryString);
        writer.write("--\r\n");
        writer.flush();
	}

}
