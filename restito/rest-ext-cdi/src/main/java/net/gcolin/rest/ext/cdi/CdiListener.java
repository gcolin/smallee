package net.gcolin.rest.ext.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class CdiListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		BeanManager bm = CDI.current().getBeanManager();
		bm.getExtension(RestExtension.class).startup(sce.getServletContext());
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
