package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;
import org.quartz.JobDataMap;

import java.util.Map;

/**
 *
 */
public abstract class HttpBodyJob extends HttpJob {
    /** Key the request will be retrieved from. */
    public static final String REQUEST_KEY = "request";

    public HttpBodyJob(HttpClient client) {
        super(client);
    }

    public HttpBodyJob() {
        super();
    }

    @Override
    protected HttpResponse createHttpResponse(String url, JobDataMap jobDataMap) {
        HttpResponse response = new HttpResponse();
        String request = getRequest(jobDataMap);
        response.setRequest(request);
        return response;
    }

    @Override
    protected abstract void makeHttpCall(HttpClient client, Map<String, Object> httpParams, Map<String, String> httpHeaders, String url, HttpResponse response);

    /**
     * @param jobDataMap job data.
     * @return data to post.
     */
    protected String getRequest(JobDataMap jobDataMap) {
        return jobDataMap.getString(REQUEST_KEY);
    }
}
