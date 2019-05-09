/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     RpcClient   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 15:15  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.rpc;

import com.google.inject.Inject;
import com.google.protobuf.Message;

import com.kennyzhu.micro.framework.HttpRequestWrapper;
import com.kennyzhu.micro.framework.OrangeContext;
import com.kennyzhu.micro.framework.protobuf.ProtobufRpcRequest;
import com.kennyzhu.micro.framework.protobuf.ProtobufRpcResponse;
import com.kennyzhu.micro.framework.protobuf.ProtobufUtil;
import com.kennyzhu.micro.framework.rpc.exception.ProtobufRpcCallExceptionDecoder;
import com.kennyzhu.micro.framework.rpc.backoff.RetryBackOffFunction;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.kennyzhu.micro.framework.jetty.RpcServlet.TYPE_JSON;
import static com.kennyzhu.micro.framework.jetty.RpcServlet.TYPE_OCTET;

public class RpcClient<RESPONSE extends Message> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private final LoadBalancer loadBalancer;
    private final String serviceName;
    private final String methodName;
    private Class<RESPONSE> responseClass;
    private int retries;
    private int timeout;
    private RetryBackOffFunction retryBackOffFunction;

    @Inject
    public RpcClient(
            LoadBalancer loadBalancer,
            String serviceName,
            String methodName,
            int retries,
            int timeout,
            final RetryBackOffFunction retryBackOffFunction,
            Class<RESPONSE> responseClass
    ) {
        this.loadBalancer = loadBalancer;
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.retries = retries;
        this.timeout = timeout;
        this.responseClass = responseClass;
        this.retryBackOffFunction = retryBackOffFunction;
    }

    // Attention: request was json , caller realize it
    // the result return was also raw string.
    public String callSynchronous(String jsonRequest, OrangeContext orangeContext)
            throws RpcCallException {
        HttpClientWrapper clientWrapper = loadBalancer.getHttpClientWrapper();
        HttpRequestWrapper balancedPost = clientWrapper.createHttpPost(this);

        //set custom headers
        if (orangeContext != null) {
            orangeContext.getProperties().forEach(balancedPost::setHeader);
        }

        balancedPost.setHeader("Content-type", TYPE_JSON);
        //TODO: fix: Temporary workaround below until go services are more http compliant
        balancedPost.setHeader("Connection", "close");
        balancedPost.setContentProvider(new StringContentProvider(jsonRequest));

        logger.debug("Sending request of size {}", jsonRequest.length());
        ContentResponse rpcResponse = clientWrapper.execute(balancedPost,
                new JsonRpcCallExceptionDecoder(), orangeContext);
        String rawResponse = rpcResponse.getContentAsString();
        logger.debug("Json response from the service: {}", rawResponse);

        return rawResponse;// JsonRpcResponse.fromString(rawResponse).getResult().getAsString();
    }

    /**
     * @deprecated use {@link #callSynchronous(Message, OrangeContext)} instead and make sure to always pass the {@link OrangeContext}
     */
    @Deprecated
    public RESPONSE callSynchronous(Message request) throws RpcCallException {
        return callSynchronous(request, null);
    }

    // orangeContext: http headers..
    public RESPONSE callSynchronous(Message request, OrangeContext orangeContext) throws RpcCallException {
        logger.debug("Sending request callSynchronous 0000000");
        HttpClientWrapper clientWrapper = loadBalancer.getHttpClientWrapper();
        HttpRequestWrapper balancedPost = clientWrapper.createHttpPost(this);
        logger.debug("Sending request callSynchronous 1111111");
        //set custom headers
        if (orangeContext != null) {
            orangeContext.getProperties().forEach(balancedPost::setHeader);
        }

        // application/octet-stream...
        balancedPost.setHeader("Content-type", TYPE_OCTET);
        //TODO: fix: Temporary workaround below until go services are more http compliant
        // http1.1 compliant, long connection disable, close when request is done..
        // commented by kenny-zhu 20190305, for not exception to communicate with micro-go server..
        // balancedPost.setHeader("Connection", "close");
        ProtobufRpcRequest pbRequest = new ProtobufRpcRequest(methodName, request);
        byte[] protobufData = pbRequest.getProtobufData();
        balancedPost.setContentProvider(new BytesContentProvider(protobufData));

        logger.debug("Sending request of size {}", protobufData.length);
        ContentResponse rpcResponse = clientWrapper.execute(balancedPost,
                new ProtobufRpcCallExceptionDecoder(), orangeContext);
        byte[] data = rpcResponse.getContent();
        logger.debug("Received a proto response of size: {}", data.length);

        //Close connection .
        return ProtobufUtil.byteArrayToProtobuf(
                new ProtobufRpcResponse(data).getPayloadData(), responseClass);
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getServiceMethodName() {
        return serviceName + "." + methodName;
    }

    public String getServiceMethodUrl() {
        return serviceName + "/" + methodName;
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean hasRetryBackOffFunction() {
        return retryBackOffFunction != null;
    }

    public RetryBackOffFunction getRetryBackOffFunction() {
        return retryBackOffFunction;
    }
}
