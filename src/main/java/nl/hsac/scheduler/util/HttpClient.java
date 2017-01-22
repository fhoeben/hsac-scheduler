package nl.hsac.scheduler.util;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Helper to make Http calls and get response.
 */
public class HttpClient {
    /** Key to use in properties/jobData to control time to get connection from pool. */
    public static final String HTTP_CONN_MANAGER_TIMEOUT_KEY = "http.conn-manager.timeout";
    /** Key to use in properties/jobData to control time to establish connection. */
    public static final String HTTP_CONNECTION_TIMEOUT_KEY = "http.connection.timeout";
    /** Key to use in properties/jobData to control time to complete request. */
    public static final String HTTP_SOCKET_TIMEOUT_KEY = "http.socket.timeout";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
    private static final int MAX_CONNECTIONS = PropertyHelper.getIntProperty("http.maxconnections.total");
    private static final int MAX_CONNECTIONS_PER_ROUTE = PropertyHelper.getIntProperty("http.maxconnections.perroute");

    private final ContentType type = ContentType.create(ContentType.TEXT_XML.getMimeType(), Consts.UTF_8);
    private static final org.apache.http.client.HttpClient HTTP_CLIENT;
    private org.apache.http.client.HttpClient httpClient;

    static {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAX_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);

        RequestConfig rc = RequestConfig.custom()
                .setConnectTimeout(PropertyHelper.getIntProperty(HTTP_CONNECTION_TIMEOUT_KEY))
                .setConnectionRequestTimeout(PropertyHelper.getIntProperty(HTTP_CONN_MANAGER_TIMEOUT_KEY))
                .setSocketTimeout(PropertyHelper.getIntProperty(HTTP_SOCKET_TIMEOUT_KEY))
                .build();
        HTTP_CLIENT = HttpClients.custom()
                .useSystemProperties()
                .disableContentCompression()
                .setDefaultRequestConfig(rc)
                .setUserAgent(HttpClient.class.getName())
                .setConnectionManager(cm)
                .build();
    }

    /**
     * Creates new.
     */
    public HttpClient() {
        httpClient = HTTP_CLIENT;
    }

    /**
     * @param url URL of service
     * @param response response pre-populated with request to send. Response content and
     *          statusCode will be filled.
     * @param headers headers for request.
     * @param parameters parameters for http client to set on request.
     */
    public void post(String url, HttpResponse response, Map<String, String> headers, Map<String, Object> parameters) {
        HttpPost methodPost = new HttpPost(url);
        setParametersAndHeaders(methodPost, parameters, headers);
        HttpEntity ent = new StringEntity(response.getRequest(), type);
        methodPost.setEntity(ent);
        getResponse(url, response, methodPost);
    }

    /**
     * @param url URL of service
     * @param response response to be filled.
     * @param headers headers for request.
     * @param parameters parameters for http client to set on request.
     */
    public void get(String url, HttpResponse response, Map<String, String> headers, Map<String, Object> parameters) {
        HttpGet method = new HttpGet(url);
        setParametersAndHeaders(method, parameters, headers);
        getResponse(url, response, method);
    }

    private void setParametersAndHeaders(HttpRequestBase method,
                                            Map<String, Object> parameters, Map<String, String> headers) {
        setParameters(method, parameters);
        setHeaders(method, headers);
    }

    private void setParameters(HttpRequestBase method, Map<String, Object> parameters) {
        if (parameters != null) {
            boolean isCustom = false;
            RequestConfig.Builder rc = RequestConfig.custom();
            Integer connectTimeout = getIntValue(parameters, HTTP_CONNECTION_TIMEOUT_KEY);
            if (connectTimeout != null) {
                rc.setConnectTimeout(connectTimeout);
                isCustom = true;
            }
            Integer socketTimeout = getIntValue(parameters, HTTP_SOCKET_TIMEOUT_KEY);
            if (socketTimeout != null) {
                rc.setSocketTimeout(socketTimeout);
                isCustom = true;
            }
            Integer connectionMgrTimeout = getIntValue(parameters, HTTP_CONN_MANAGER_TIMEOUT_KEY);
            if (connectionMgrTimeout != null) {
                rc.setConnectionRequestTimeout(connectionMgrTimeout);
                isCustom = true;
            }
            if (isCustom) {
                method.setConfig(rc.build());
            }
        }
    }

    private Integer getIntValue(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        return (Integer) value;
    }

    private void setHeaders(HttpRequestBase method, Map<String, String> headers) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                if (value != null) {
                    method.setHeader(key, value);
                }
            }
        }
    }

    private void getResponse(String url, HttpResponse response, HttpRequestBase method) {
        try {
            org.apache.http.HttpResponse resp = getHttpResponse(method);
            int returnCode = resp.getStatusLine().getStatusCode();
            LOGGER.debug("Call returned status code: {}", returnCode);
            response.setStatusCode(returnCode);
            String result = EntityUtils.toString(resp.getEntity());
            LOGGER.trace("Call returned: {}", result);
            response.setResponse(result);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get response from: " + url, e);
        } finally {
            method.reset();
        }
    }

    /**
     * Executes method.
     * @param method method to be executed.
     * @return result.
     * @throws IOException if call could not be made.
     */
    protected org.apache.http.HttpResponse getHttpResponse(HttpRequestBase method) throws IOException {
        LOGGER.trace("Executing call");
        org.apache.http.HttpResponse result = httpClient.execute(method);
        LOGGER.trace("Executed call");
        return result;
    }

    /**
     * @return http client in use.
     */
    public org.apache.http.client.HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * @param anHttpClient http client to use.
     */
    public void setHttpClient(org.apache.http.client.HttpClient anHttpClient) {
        httpClient = anHttpClient;
    }
}
