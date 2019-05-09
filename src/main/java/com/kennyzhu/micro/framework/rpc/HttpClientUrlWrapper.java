package com.kennyzhu.micro.framework.rpc;

import com.google.inject.Inject;
import com.kennyzhu.micro.framework.HttpRequestWrapper;
import com.kennyzhu.micro.framework.OrangeContext;
import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallExceptionDecoder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static net.logstash.logback.marker.Markers.append;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     HttpClientUrlWrapper   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/9 9:50  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public class HttpClientUrlWrapper extends HttpClientWrapper {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUrlWrapper.class);

    @Inject
    public HttpClientUrlWrapper(ServiceProperties serviceProps, HttpClient httpClient) {
        super(serviceProps, httpClient);
    }

    @Override
    public HttpRequestWrapper createHttpPost(RpcClient client)
            throws RpcCallException {
        this.client = client;
        ServiceEndpoint instance = loadBalancer.getHealthyInstance();
        if (instance == null) {
            throw new RpcCallException(RpcCallException.Category.InternalServerError,
                    "No available instance of " + loadBalancer.getServiceName()).
                    withSource(serviceProps.getServiceName());
        }
        return new HttpRequestWrapper("POST", instance, client.getMethodName() );
    }

    @Override
    protected HttpRequestWrapper createHttpPost(HttpRequestWrapper previous, List<ServiceEndpoint> triedEndpoints)
            throws RpcCallException {
        ServiceEndpoint instance = loadBalancer.getHealthyInstanceExclude(triedEndpoints);
        if (instance == null) {
            throw new RpcCallException(RpcCallException.Category.InternalServerError,
                    "RpcCallException calling " + loadBalancer.getServiceName() + ", no available instance").
                    withSource(serviceProps.getServiceName());
        }
        //TODO: There may still be a problem where retries are setting chunked encoding
        // or the content-length gets munged
        HttpRequestWrapper retval =  new HttpRequestWrapper("POST", instance, client.getMethodName());
        retval.setHeaders(previous.getHeaders());
        retval.setContentProvider(previous.getContentProvider());
        return retval;
    }
}
