/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     HttpClientWrapper   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 15:12  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.rpc;

import com.google.inject.Inject;

import com.kennyzhu.micro.framework.HttpRequestWrapper;
import com.kennyzhu.micro.framework.OrangeContext;
import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import com.kennyzhu.micro.framework.metrics.GoTimer;
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


public class HttpClientWrapper {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientWrapper.class);

    protected ServiceProperties serviceProps;
    protected LoadBalancer loadBalancer;
    protected HttpClient httpClient;
    // protected RpcClientMetrics rpcClientMetrics;
    // Trace.
    protected RpcClient client;

    @Inject
    public HttpClientWrapper(ServiceProperties serviceProps, HttpClient httpClient) {
        this.serviceProps = serviceProps;
        this.httpClient = httpClient;
    }

    public HttpRequestWrapper createHttpPost(RpcClient client)
            throws RpcCallException {
        this.client = client;
        ServiceEndpoint instance = loadBalancer.getHealthyInstance();
        if (instance == null) {
            throw new RpcCallException(RpcCallException.Category.InternalServerError,
                    "No available instance of " + loadBalancer.getServiceName()).
                    withSource(serviceProps.getServiceName());
        }
        return new HttpRequestWrapper("POST", instance);
    }

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
        HttpRequestWrapper retval =  new HttpRequestWrapper("POST", instance);
        retval.setHeaders(previous.getHeaders());
        retval.setContentProvider(previous.getContentProvider());
        return retval;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public ContentResponse execute(HttpRequestWrapper request, RpcCallExceptionDecoder decoder,
                                   OrangeContext orangeContext)
            throws RpcCallException {
        ContentResponse retval = null;
        List<ServiceEndpoint> triedEndpoints = new ArrayList<>();
        RpcCallException lastException = null;
        int lastStatusCode;
        int tryCount = 0;
        do {
            triedEndpoints.add(request.getServiceEndpoint());
            // GoTimer methodTimer = getMethodTimer();
            // long startTime = methodTimer.start();
            try {
                Marker logMarker = append("serviceMethod", request.getMethod())
                        .and(append("serviceEndpoint", request.getServiceEndpoint()));
                logger.debug(logMarker,
                        "Sending http request to {}", request.getServiceEndpoint());

                // TODO: add trace..
                retval = request.newRequest(httpClient).timeout(client.getTimeout(),
                        TimeUnit.MILLISECONDS).send();
                logger.debug(logMarker, "Http send completed");
                lastStatusCode = retval.getStatus();
            } catch (TimeoutException timeout) {
                lastStatusCode = RpcCallException.Category.RequestTimedOut.getHttpStatus();
                lastException = new RpcCallException(RpcCallException.Category.RequestTimedOut, "Http-client timeout");
                //TODO: RequestTimedOut should be retried as long as there is time budget left
                logger.info(getRemoteMethod(), "Caught TimeoutException executing request");
            } catch (Exception ex) {
                lastStatusCode = RpcCallException.Category.InternalServerError.getHttpStatus();
                logger.debug(getRemoteMethod(), "Caught exception executing request", ex);
            }

            logger.debug("Response status code = {}", lastStatusCode);

            if (responseWasSuccessful(decoder, retval, lastStatusCode)) {
                // methodTimer.recordSuccess(startTime);
                request.getServiceEndpoint().requestComplete(true);
                return retval;
            } else {
                // methodTimer.recordFailure(startTime);
                // 4xx errors should not change circuit-breaker state
                request.getServiceEndpoint().requestComplete(lastStatusCode < 500);
                if (lastStatusCode != RpcCallException.Category.RequestTimedOut.getHttpStatus()) {
                    lastException = decoder.decodeException(retval);
                    if (lastException != null && !lastException.isRetriable()) {
                        throw lastException;
                    }
                }
                if (tryCount < client.getRetries()) {
                    if (client.hasRetryBackOffFunction()) {
                        client.getRetryBackOffFunction().execute(tryCount);
                    }
                    request = createHttpPost(request, triedEndpoints);
                }
            }
            tryCount++;
        } while (request != null && tryCount <= client.getRetries());

        if (lastException == null) {
            throw new RpcCallException(RpcCallException.Category.fromStatus(lastStatusCode),
                    "Null response in execute").withSource(serviceProps.getServiceName());
        } else {
            throw lastException;
        }
    }

    // call failed process in app layer.
    private boolean responseWasSuccessful(RpcCallExceptionDecoder decoder,
                                          ContentResponse response, int lastStatusCode)  {
        return lastStatusCode == 200 && response != null && response.getContent().length > 0;
    }

    private GoTimer getMethodTimer() {
        // to do , add servicename.method metrics.
        return new GoTimer("");
    }

    protected Marker getRemoteMethod() {
        return append("method", client.getServiceMethodName());
    }

}
