package sample;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.gcolin.di.atinject.web.RequestScoped;

@Path("service")
@RequestScoped
public class Service {

	@Inject
	private Session session;

	@Inject
	private Application global;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String incr() {
		return "counter session: " + session.next() + "\ncounter global : " + global.next();
	}

}
