package sample;

import net.gcolin.common.collection.Collections2;

import java.util.Set;

import javax.ws.rs.core.Application;

public class RsApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return Collections2.toSet(Service.class);
  }
  
  @Override
  public Set<Object> getSingletons() {
    return Collections2.toSet(new ConstraintViolationExceptionMapper());
  }
  
}
