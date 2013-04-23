package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpHostConnectException;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Job that makes Http call. HttpResponse is stored in jobExecutionContext.
 */
public abstract class HttpJob extends JobBase {
    /** KEY used to determine URL. */
    public static final String URL_KEY = "url";
    private final HttpClient client = new HttpClient();

    @Override
    protected void executeImpl(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        String url = getUrl(jobDataMap);
        getLog().debug("Calling: {}", url);
        HttpResponse response = createHttpResponse(url, jobDataMap);
        jobExecutionContext.setResult(response);
        try {
            client.get(url, response);
        } catch (RuntimeException e) {
            handleGetException(jobExecutionContext, url, response, e);
        }
        try {
            handleResponse(jobExecutionContext, url, response);
        } catch (RuntimeException e) {
            getLog().warn("Did not get a valid response from {}, got: '{}'", url, response.getResponse(), e);
            throw new JobExecutionException(e);
        }
    }

    /**
     * @param jobDataMap job data.
     * @return url the request will be sent to.
     */
    protected String getUrl(JobDataMap jobDataMap) {
        return jobDataMap.getString(URL_KEY);
    }

    /**
     * @param url url to be called.
     * @param jobDataMap job data.
     * @return http response to be completed by httpClient.
     */
    protected abstract HttpResponse createHttpResponse(String url, JobDataMap jobDataMap);

    /**
     * Handles exception while getting the response.
     *
     * @param context job context.
     * @param url url used.
     * @param response the value of response.
     * @param e exception caught
     * @throws JobExecutionException if processing should not continue.
     */
    protected void handleGetException(JobExecutionContext context, String url, HttpResponse response, RuntimeException e) throws JobExecutionException {
        Throwable cause = e.getCause();
        if (cause instanceof HttpHostConnectException) {
            String msg = String.format("%s: '%s'", e.getMessage(), cause.getMessage());
            getLog().warn(msg);
            throw new JobExecutionException(msg, e);
        } else {
            getLog().error("Error", e);
        }
        throw new JobExecutionException(e);
    }

    /**
     * Handles response.
     * @param context job context.
     * @param url url used.
     * @param response after request completed.
     */
    protected void handleResponse(JobExecutionContext context, String url, HttpResponse response) {
        if (HttpStatus.SC_OK == response.getStatusCode()) {
            String msg = String.format("%s response status code: OK (%s)", url, response.getStatusCode());
            getLog().info(msg);
            reportStatus(context, true, msg);
        } else {
            String msg = String.format("%s response status code: %s", url, response.getStatusCode());
            getLog().warn(msg);
            reportStatus(context, false, msg);
        }
        getLog().debug("Response content:\n{}", response.getResponse());
    }
}
