package nl.hsac.scheduler.web;

import nl.hsac.scheduler.util.StatusManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * Servlet to serve statuses as known by StatusManager.
 */
public class StatusCheckServlet  extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String version = VersionServlet.getVersion(request.getServletContext());

        Collection<String> results = StatusManager.getInstance().getStatuses();
        fillResponse(response, version, results);
    }

    private void fillResponse(HttpServletResponse response, String version, Collection<String> results) throws IOException {
        String status;
        if (allOk(results)) {
            status = StatusManager.OK;
        } else {
            status = StatusManager.NOK;
        }
        String title = String.format("Status Check: %s", status);

        response.setContentType("text/html; charset=UTF-8");

        writeHtml(response, title, version, results);

        // set status late to ensure browsers don't only use the code,
        // but also retrieve HTML
        if (StatusManager.OK.equals(status)) {
            response.setStatus(200);
        } else {
            response.setStatus(500);
        }
    }

    private void writeHtml(HttpServletResponse response, String title, String version, Collection<String> results) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.format("<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"%s\">", response.getContentType());
        writer.format("<title>%s</title>", title);
        writer.append("</head><body>");
        writer.format("<h1>%s</h1>", title);
        writer.format("<p>Version: %s</p>", version);
        writer.append("<ul>");
        for (String result : results) {
            writer.append("<li>");
            if (notOk(result)) {
               writer.format("<b>%s</b>", result);
            } else {
               writer.append(result);
            }
            writer.append("</li>");
        }
        writer.append("</ul>");
        writer.append("</body></html>");
        writer.flush();
    }

    private boolean allOk(Collection<String> results) {
        boolean allOk = true;
        for(String result : results) {
            if (notOk(result)) {
                allOk = false;
            }
        }
        return allOk;
    }

    private boolean notOk(String result) {
        return result == null
                   || !result.startsWith(StatusManager.OK);
    }
}
