package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;
import org.quartz.JobDataMap;

import java.util.Map;

/**
 * Job to perform HTTP GET.
 */
public class HttpGetJob extends HttpJob {

    @Override
    protected HttpResponse createHttpResponse(String url, JobDataMap jobDataMap) {
        HttpResponse response = new HttpResponse();
        response.setRequest(url);
        return response;
    }

    @Override
    protected void makeHttpCall(HttpClient client,  Map<String, Object> httpParams, String url, HttpResponse response) {
        client.get(url, response, httpParams);
    }
}
