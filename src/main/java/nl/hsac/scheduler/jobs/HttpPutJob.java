package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;

import java.util.Map;

/**
 * Job to perform HTTP PUT.
 * Data to post is retrieved from key: request.
 */
public class HttpPutJob extends HttpBodyJob {

    /**
     * Creates new, with new HttpClient.
     */
    public HttpPutJob() {
        super();
    }

    /**
     * Creates new.
     * @param client http client to use.
     */
    public HttpPutJob(HttpClient client) {
        super(client);
    }

    @Override
    protected void makeHttpCall(HttpClient client,  Map<String, Object> httpParams, Map<String, String> httpHeaders, String url, HttpResponse response) {
        client.put(url, response, httpHeaders, httpParams);
    }

}
