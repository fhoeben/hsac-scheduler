package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;

import java.util.Map;

/**
 * Job to perform HTTP POST.
 * Data to post is retrieved from key: request.
 */
public class HttpPostJob extends HttpBodyJob {

    /**
     * Creates new, with new HttpClient.
     */
    public HttpPostJob() {
        super();
    }

    /**
     * Creates new.
     * @param client http client to use.
     */
    public HttpPostJob(HttpClient client) {
        super(client);
    }

    @Override
    protected void makeHttpCall(HttpClient client,  Map<String, Object> httpParams, Map<String, String> httpHeaders, String url, HttpResponse response) {
        client.post(url, response, httpHeaders, httpParams);
    }

}
