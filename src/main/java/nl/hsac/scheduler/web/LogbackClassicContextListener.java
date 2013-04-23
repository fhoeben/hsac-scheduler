package nl.hsac.scheduler.web;

import ch.qos.logback.classic.LoggerContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.LoggerFactory;

/**
 * Context listener to allow clean-up/setup on application stop/start.
 */
@WebListener
public class LogbackClassicContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // no work (yet).
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // prevent memory leaks by JMX access to logging config
        // see http://logback.qos.ch/manual/jmxConfig.html#leak
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.stop();
    }
   
}
