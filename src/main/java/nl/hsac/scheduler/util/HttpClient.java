package nl.hsac.scheduler.util;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Helper to make Http calls and get response.
 */
public class HttpClient {
    private static final int MAX_CONNECTIONS = PropertyHelper.getIntProperty("http.maxconnections.total");
    private static final int MAX_CONNECTIONS_PER_ROUTE = PropertyHelper.getIntProperty("http.maxconnections.perroute");

    private final ContentType type = ContentType.create(ContentType.TEXT_XML.getMimeType(), Consts.UTF_8);
    private static final org.apache.http.client.HttpClient HTTP_CLIENT;
    private org.apache.http.client.HttpClient httpClient;

    static {
        SystemDefaultHttpClient backend = new SystemDefaultHttpClient();
        setIntParam(backend, "http.connection.timeout");
        setIntParam(backend, "http.socket.timeout");
        setLongParam(backend, "http.conn-manager.timeout");

        PoolingClientConnectionManager cm = (PoolingClientConnectionManager) backend.getConnectionManager();
        cm.setMaxTotal(MAX_CONNECTIONS);
        cm.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        HTTP_CLIENT = new DecompressingHttpClient(backend);
    }

    /**
     * Sets a http client property based on value in this application's property file.
     * @param client client to set parameter on.
     * @param key key for parameter, the value will be retrieved from property with same name.
     */
    private static void setIntParam(org.apache.http.client.HttpClient client, String key) {
        HttpParams clientParams = client.getParams();
        int value = PropertyHelper.getIntProperty(key);
        clientParams.setParameter(key, value);
    }

    /**
     * Sets a http client property based on value in this application's property file.
     * @param client client to set parameter on.
     * @param key key for parameter, the value will be retrieved from property with same name.
     */
    private static void setLongParam(org.apache.http.client.HttpClient client, String key) {
        HttpParams clientParams = client.getParams();
        long value = PropertyHelper.getLongProperty(key);
        clientParams.setParameter(key, value);
    }

    /**
     * Creates new.
     */
    public HttpClient() {
        httpClient = HTTP_CLIENT;
        httpClient.getParams().setParameter("http.useragent", getClass().getName());
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
            HttpParams methodParams = method.getParams();
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                methodParams.setParameter(key, value);
            }
        }
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
            org.apache.http.HttpResponse resp = getHttpResponse(url, method);
            int returnCode = resp.getStatusLine().getStatusCode();
            response.setStatusCode(returnCode);
            String result = EntityUtils.toString(resp.getEntity());
            response.setResponse(result);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get response from: " + url, e);
        } finally {
            method.reset();
        }
    }

    /**
     * Executes method.
     * @param url url to be called.
     * @param method method to be executed.
     * @return result.
     * @throws IOException if call could not be made.
     */
    protected org.apache.http.HttpResponse getHttpResponse(String url, HttpRequestBase method) throws IOException {
        return httpClient.execute(method);
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
