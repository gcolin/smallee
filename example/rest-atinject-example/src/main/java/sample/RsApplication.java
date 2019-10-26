package sample;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import net.gcolin.common.collection.Collections2;
import net.gcolin.di.atinject.web.ApplicationScoped;

@ApplicationScoped
@ApplicationPath("/rest")
public class RsApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    return Collections2.toSet(Service.class);
  }
  
}
