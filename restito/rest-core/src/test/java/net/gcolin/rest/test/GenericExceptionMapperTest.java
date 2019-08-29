package net.gcolin.rest.test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.junit.Assert;
import org.junit.Test;

import net.gcolin.common.reflect.Reflect;
import net.gcolin.common.reflect.TypedInvocationHandler;

public class GenericExceptionMapperTest {

	public static class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
		
		@Override
		public Response toResponse(T exception) {
			return null;
		}
		
	}
	
	public static class ExceptionMapperImpl extends AbstractExceptionMapper<IllegalArgumentException> {
		
	}
	
	@Test
	public void test() {
		Class<?> type = TypedInvocationHandler.getRealType(new ExceptionMapperImpl());
	    Class<?> exType = Throwable.class;

	    for(Method method: type.getMethods()) {
	    	if(method.getName().equals("toResponse") && method.getParameterCount() == 1) {
	    		Type t = method.getGenericParameterTypes()[0];
	    		if(t instanceof TypeVariable) {
	    			exType = Reflect.toClass(Reflect.getType(ExceptionMapper.class, type, t));
	    		} else {
	    			exType = Reflect.toClass(t);
	    		}
	    		break;
	    	}
	    }
	    
	    Assert.assertEquals(IllegalArgumentException.class, exType);
	}
	
}
