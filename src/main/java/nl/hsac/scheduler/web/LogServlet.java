package nl.hsac.scheduler.web;

import nl.hsac.scheduler.util.FileUtil;
import nl.hsac.scheduler.util.LogHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * Servlet to show log contents.
 */
public class LogServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final LogHelper LOG_HELPER = new LogHelper();

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException {
        httpServletResponse.setContentType("text/plain");
        File file = LOG_HELPER.getLogFile(getLog(), getAppenderName());
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        FileUtil.copy(in, httpServletResponse.getOutputStream());
    }

    private ch.qos.logback.classic.Logger getLog() {
        return (ch.qos.logback.classic.Logger) LOG;
    }

    private String getAppenderName() {
        return getInitParameter("appender");
    }
}
