package sample;

import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import net.gcolin.common.lang.Strings;

public class ConstraintViolationExceptionMapper implements
		ExceptionMapper<ConstraintViolationException> {

	@Override
	public Response toResponse(ConstraintViolationException e) {
	  return Response.status(Response.Status.BAD_REQUEST).entity(
				Strings.join(e.getConstraintViolations(), x -> getPropertyName(x.getPropertyPath())+" "+ x.getMessage(), ", ")).build();
	}
	
	private String getPropertyName(Path p){
		String name = null;
		for(Path.Node path:p){
			if(path.getName()!=null){
				name = path.getName();
			}
		}
		return name;
	}

}
