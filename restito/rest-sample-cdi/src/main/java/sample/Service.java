package sample;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("service")
@RequestScoped
public class Service {

  @Inject
  private Session session;
  
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String incr() {
    return "counter : " + session.next();
  }
  
  
}
