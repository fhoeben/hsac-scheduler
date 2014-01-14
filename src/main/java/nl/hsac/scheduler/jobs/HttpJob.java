package nl.hsac.scheduler.jobs;

import nl.hsac.scheduler.util.HttpClient;
import nl.hsac.scheduler.util.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.conn.HttpHostConnectException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * Job that makes Http call. HttpResponse is stored in jobExecutionContext.
 * No new calls will be made, until previous call returned a response (or timed out).
 */
@DisallowConcurrentExecution
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
            Map<String, Object> httpParams = getHttpParams(jobDataMap);
            Map<String, String> httpHeaders = getHeaders(jobDataMap);
            makeHttpCall(client, httpParams, httpHeaders, url, response);
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

    protected Map<String, String> getHeaders(JobDataMap jobDataMap) {
        Map<String, String> headers = null;
        for (Map.Entry<String, Object> entry : jobDataMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith("header:")) {
                if (headers == null) {
                    headers = new HashMap<String, String>();
                }
                headers.put(key.substring("header:".length()), value.toString());
            }
        }
        return headers;
    }

    protected Map<String, Object> getHttpParams(JobDataMap jobDataMap) {
        Map<String, Object> result = new HashMap<String, Object>();
        copyIntIfPresent(jobDataMap, result, AllClientPNames.CONNECTION_TIMEOUT);
        copyIntIfPresent(jobDataMap, result, AllClientPNames.SO_TIMEOUT);
        return result;
    }

    private void copyIntIfPresent(JobDataMap source, Map<String, Object> target, String key) {
        Object value = source.get(key);
        if (value != null) {
            String valueStr = (String) value;
            target.put(key, Integer.valueOf(valueStr));
        }
    }

    /**
     * Performs actual http call.
     * @param client client to make call with.
     * @param httpParams override parameters for call.
     * @param httpHeaders headers for call.
     * @param url url to call.
     * @param response response to fill.
     */
    protected abstract void makeHttpCall(HttpClient client, Map<String, Object> httpParams, Map<String, String> httpHeaders, String url, HttpResponse response);

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
        Logger log = getLog();
        if (cause instanceof HttpHostConnectException
                || cause instanceof SocketTimeoutException) {
            String msg = String.format("%s: '%s'", e.getMessage(), cause.getMessage());
            if (log.isDebugEnabled()) {
                log.warn(msg, e);
            } else {
                log.warn(msg);
            }
            throw new JobExecutionException(msg, e);
        } else {
            log.error("Error", e);
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
