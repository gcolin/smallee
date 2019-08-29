package sample;

import net.gcolin.common.collection.Collections2;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationScoped
@ApplicationPath("/rest")
public class RsApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return Collections2.toSet(Service.class);
  }
  
}
