package nl.hsac.scheduler.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.lang.invoke.MethodHandles;

/**
 * Context listener to log version on application stop/start.
 */
@WebListener
public class VersionLoggingContextListener implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String version = "unknown";
    private String applicationName = "application";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        applicationName = sc.getServletContextName();
        version = VersionServlet.getVersion(sc);
        LOG.info("Started {} version {}", applicationName, version);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("Stopped {} version {}", applicationName, version);
    }

}
