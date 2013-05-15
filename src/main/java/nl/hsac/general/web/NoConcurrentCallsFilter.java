package nl.hsac.general.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Filter to prevent a path from serving more than one concurrent requests at
 * any time.
 */
@SuppressWarnings("PMD.LoggerIsNotStaticFinal")
public class NoConcurrentCallsFilter implements Filter {
    private static final ConcurrentMap<String, NoConcurrentCallsFilter>
            FILTERS = new ConcurrentHashMap<String, NoConcurrentCallsFilter>();
    // temporary unavailable
    private static final int BUSY_HTTP_STATUS = 503;

    private Logger log;
    private String name;
    private ConcurrentMap<String, Date> runningRequests;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        runningRequests = new ConcurrentHashMap<String, Date>();
        String className = getClass().getName();
        name = filterConfig.getFilterName();
        log = LoggerFactory.getLogger(className + "." + name);
        FILTERS.put(name, this);
        log.debug("Initialized");
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        String path = getPath(request);
        boolean shouldClear = false;
        Date requestDate = new Date();
        try {
            Date runningDate = runningRequests.putIfAbsent(path, requestDate);
            if (runningDate == null) {
                log.debug("Allowing request for: {} to proceed.", path);
                shouldClear = true;
                chain.doFilter(request, response);
            } else {
                log.info("Not forwarding request for {}, "
                        + "previous request, from {}, is still running.",
                        path, runningDate);
                informClient(path, runningDate, response);
            }
        } finally {
            if (shouldClear) {
                runningRequests.remove(path);
                log.debug("Request for {} completed, "
                        + "next request will be allowed to proceed.",
                        path);
            }
        }
    }

    @Override
    public void destroy() {
        FILTERS.remove(name, this);
        runningRequests.clear();
        log.debug("Destroyed");
    }

    /**
     * @return (copy of) requests currently in progress.
     */
    public Map<String, Date> getRunningRequests() {
        return new HashMap<String, Date>(runningRequests);
    }

    /**
     * @return (copy of) requests of all filters currently in progress.
     */
    public static Map<String, Date> getAllRunningRequests() {
        Map<String, Date> result = new HashMap<String, Date>();
        for (NoConcurrentCallsFilter filter : FILTERS.values()) {
            Map<String, Date> requests = filter.getRunningRequests();
            result.putAll(requests);
        }
        return result;
    }

    private void informClient(String path, Date runningDate,
                              ServletResponse response) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        String title = String.format("Request to %s blocked.", path);
        PrintWriter writer = response.getWriter();
        try {
            writer.format("<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"%s\">",
                    response.getContentType());
            writer.format("<title>%s</title>", title);
            writer.append("</head><body>");
            writer.format("<h1>%s</h1>", title);
            writer.format("<p>Currently working on request received on: %s</p>",
                    runningDate);
            writer.append("</body></html>");
        } finally {
            writer.flush();
        }
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setStatus(BUSY_HTTP_STATUS);
        }
    }

    private String getPath(ServletRequest request) {
        String path;
        if (request instanceof HttpServletRequest) {
            path = ((HttpServletRequest) request).getRequestURI();
        } else {
            path = request.getServletContext().getContextPath();
        }
        return path;
    }
}
