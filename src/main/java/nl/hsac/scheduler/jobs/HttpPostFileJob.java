package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;

import java.util.Map;

/**
 * Job that posts the contents of a (UTF-8) file on the classpath.
 */
public class HttpPostFileJob extends HttpFileBodyJob {

    /**
     * Creates new, with new HttpClient.
     */
    public HttpPostFileJob() {
        super();
    }

    /**
     * Creates new.
     * @param client http client to use.
     */
    public HttpPostFileJob(HttpClient client) {
        super(client);
    }

    @Override
    protected void makeHttpCall(HttpClient client, Map<String, Object> httpParams, Map<String, String> httpHeaders, String url, HttpResponse response) {
        client.post(url, response, httpHeaders, httpParams);
    }
}
