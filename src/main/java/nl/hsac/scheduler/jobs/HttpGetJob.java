package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpResponse;
import org.quartz.JobDataMap;

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
}
