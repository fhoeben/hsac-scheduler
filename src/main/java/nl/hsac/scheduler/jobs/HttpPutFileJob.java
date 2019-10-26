package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;

import java.util.Map;

/**
 * Job that puts the contents of a (UTF-8) file on the classpath.
 */
public class HttpPutFileJob extends HttpFileBodyJob {

    /**
     * Creates new, with new HttpClient.
     */
    public HttpPutFileJob() {
        super();
    }

    /**
     * Creates new.
     * @param client http client to use.
     */
    public HttpPutFileJob(HttpClient client) {
        super(client);
    }

    @Override
    protected void makeHttpCall(HttpClient client, Map<String, Object> httpParams, Map<String, String> httpHeaders, String url, HttpResponse response) {
        client.post(url, response, httpHeaders, httpParams);
    }
}
