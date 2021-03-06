package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;
import org.quartz.JobDataMap;

import java.util.Map;

/**
 * Job to perform HTTP GET.
 */
public class HttpGetJob extends HttpJob {

    /**
     * Creates new, with new HttpClient.
     */
    public HttpGetJob() {
        super();
    }

    /**
     * Creates new.
     * @param client http client to use.
     */
    public HttpGetJob(HttpClient client) {
        super(client);
    }

    @Override
    protected HttpResponse createHttpResponse(String url, JobDataMap jobDataMap) {
        HttpResponse response = new HttpResponse();
        response.setRequest(url);
        return response;
    }

    @Override
    protected void makeHttpCall(HttpClient client,  Map<String, Object> httpParams, Map<String, String> httpHeaders, String url, HttpResponse response) {
        client.get(url, response, httpHeaders, httpParams);
    }
}
