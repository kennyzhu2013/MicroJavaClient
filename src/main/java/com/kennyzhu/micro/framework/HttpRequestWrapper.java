/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     HttpRequestWrapper   
 *  * @package    com.kennyzhu.micro.framework  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 15:39  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework;

import com.kennyzhu.micro.framework.rpc.ServiceEndpoint;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.api.Request;
import org.slf4j.MDC;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class HttpRequestWrapper {
    private URI uri;
    private String method;
    private ServiceEndpoint instance;
    private ContentProvider contentProvider;
    private Map<String, String> headers = new HashMap<>();

    public HttpRequestWrapper(String method, ServiceEndpoint instance) {
        this.method = method;
        this.instance = instance;

        setUri(URI.create("http://" + instance.getHostAndPort() + "/"));
        setHeader("X-Correlation-Id", MDC.get(OrangeContext.CORRELATION_ID));
    }

    public HttpRequestWrapper(String method, ServiceEndpoint instance, String urlSuffix) {
        this.method = method;
        this.instance = instance;

        setUri(URI.create("http://" + instance.getHostAndPort() + "/" + urlSuffix));
        setHeader("X-Correlation-Id", MDC.get(OrangeContext.CORRELATION_ID));
    }

    public Request newRequest(HttpClient httpClient) {
        Request request = httpClient.newRequest(uri);
        request.content(contentProvider).method(method);

        for (String key : headers.keySet()) {
            if (! "User-Agent".equals(key)) {
                request.header(key, headers.get(key));
            }
        }

        return request;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getMethod() {
        return method;
    }

    public ServiceEndpoint getServiceEndpoint() {
        return instance;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public void setContentProvider(ContentProvider contentProvider) {
        this.contentProvider = contentProvider;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public ContentProvider getContentProvider() {
        return contentProvider;
    }
}
