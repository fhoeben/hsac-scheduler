package nl.hsac.scheduler.util;

import org.apache.http.HttpStatus;

/**
 * Wrapper around HTTP response (and request).
 */
public class HttpResponse {
    private String request;
    private String response;
    private int statusCode;

    /**
     * @throws RuntimeException if no valid response is available
     */
    public void validResponse() {
        if (statusCode == HttpStatus.SC_NOT_IMPLEMENTED) {
            throw new RuntimeException("The method is not implemented by this URI");
        }
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            throw new RuntimeException("No content available for this URI");
        }
    }

    /**
     * @return the request
     */
    public String getRequest() {
        return request;
    }

    /**
     * @param aRequest the request to set
     */
    public void setRequest(String aRequest) {
        request = aRequest;
    }

    /**
     * @return the response
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param aResponse the response to set
     */
    public void setResponse(String aResponse) {
        response = aResponse;
    }

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * @param aStatusCode the statusCode to set
     */
    public void setStatusCode(int aStatusCode) {
        statusCode = aStatusCode;
    }
}
