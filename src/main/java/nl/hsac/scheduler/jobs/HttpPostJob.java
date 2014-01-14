package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;
import org.quartz.JobDataMap;

import java.util.Map;

/**
 * Job to perform HTTP POST.
 * Data to post is retrieved from key: request.
 */
public class HttpPostJob extends HttpJob {
    /** Key the request will be retrieved from. */
    public static final String REQUEST_KEY = "request";

    @Override
    protected HttpResponse createHttpResponse(String url, JobDataMap jobDataMap) {
        HttpResponse response = new HttpResponse();
        String request = getRequest(jobDataMap);
        response.setRequest(request);
        return response;
    }

    @Override
    protected void makeHttpCall(HttpClient client,  Map<String, Object> httpParams, Map<String, String> httpHeaders, String url, HttpResponse response) {
        client.post(url, response, httpHeaders, httpParams);
    }

    /**
     * @param jobDataMap job data.
     * @return data to post.
     */
    protected String getRequest(JobDataMap jobDataMap) {
        return jobDataMap.getString(REQUEST_KEY);
    }
}
