package sample;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("service")
public class Service {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello() {
    return "hello Restito";
  }
  
  @GET
  @Path("jsp")
  @Produces(MediaType.TEXT_HTML)
  public String hellojsp() {
    return "/WEB-INF/hello.jsp";
  }
  
  @GET
  @Path("validate")
  public String hello(@QueryParam("name") @NotNull String name) {
    return "hello "+name;
  }
  
  @GET
  @Path("cars")
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public List<Car> cars() {
    Car fordf150 = new Car();
    fordf150.setHorsePower(450);
    fordf150.setName("Ford F-150");
    fordf150.setPrice(26540);
    fordf150.setYear(2017);
    
    Car mazda3 = new Car();
    mazda3.setHorsePower(184);
    mazda3.setName("Mazda3");
    mazda3.setPrice(16945);
    mazda3.setYear(2015);
    return Arrays.asList(fordf150, mazda3);
  }
  
}
