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

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.kennyzhu.micro.framework.rpc.ServiceEndpoint;
//import org.eclipse.jetty.client.HttpClient;
// import org.eclipse.jetty.client.api.ContentProvider;
//import org.eclipse.jetty.client.api.Request;
import com.github.kevinsawicki.http.HttpRequest;
import org.slf4j.MDC;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class HttpRequestWrapper {
    private String uri;
    private String method;
    private ServiceEndpoint instance;
    private String contentProvider;
    private Map<String, String> headers = new HashMap<>();

    public HttpRequestWrapper(String method, ServiceEndpoint instance) {
        this.method = method;
        this.instance = instance;

        setUri("http://" + instance.getHostAndPort() + "/");
        setHeader("X-Correlation-Id", MDC.get(OrangeContext.CORRELATION_ID));
        setHeader("X-Media-Server", MDC.get(OrangeContext.RPC_MEDIA_SERVER));
    }

    public HttpRequestWrapper(String method, ServiceEndpoint instance, String urlSuffix) {
        this.method = method;
        this.instance = instance;

        setUri("http://" + instance.getHostAndPort() + "/" + urlSuffix);
        setHeader("X-Correlation-Id", MDC.get(OrangeContext.CORRELATION_ID));
        setHeader("X-Media-Server", MDC.get(OrangeContext.RPC_MEDIA_SERVER));
    }

    // only support get and post here..
    public HttpRequest newRequest() {
        HttpRequest request = HttpRequest.post(uri);
//        if ( method == "GET" ) {
//            request = HttpRequest.get(uri);
//        } else {
//            request = HttpRequest.post(uri);
//        }
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

    public void setUri(String uri) {
        this.uri = uri;
    }
    public String getUri() {
         return this.uri;
    }

    public void setContentProvider(String contentProvider) {
        this.contentProvider = contentProvider;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getContentProvider() {
        return contentProvider;
    }
}
