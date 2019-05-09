package com.kennyzhu.micro.framework.rpc;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     RpcClientBuilder   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/8 14:33  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.protobuf.Message;
import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import com.kennyzhu.micro.framework.rpc.backoff.RetryBackOffFunction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClientBuilder<RESPONSE extends Message> {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientBuilder.class);

    public final static int DEFAULT_RETRIES = 1;
    public final static int DEFAULT_RESPONSE_TIMEOUT = 1000;

    private final Injector injector;
    private String serviceName;
    private String methodName;
    private int retries;
    private RetryBackOffFunction retryBackOffFunction;
    private int timeout;
    private Class<RESPONSE> responseClass;

    // bind the master injector...
    @Inject
    public RpcClientBuilder(Injector injector) {
        this.injector = injector;
        initialize();
    }

    void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Modify retry policy from default
     */
    public RpcClientBuilder<RESPONSE> withRetries(int retries) {
        this.retries = retries;
        return this;
    }

    /**
     * Modify timeout policy from default
     * @param timeout milliseconds
     */
    public RpcClientBuilder<RESPONSE> withTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * RPC call retry timeout function implementation
     *
     * @param retryBackOffFunction - implementation of final RpcClient.RetryBackOffFunction
     */
    public RpcClientBuilder<RESPONSE> withRetryBackOff(final RetryBackOffFunction retryBackOffFunction) {
        this.retryBackOffFunction = retryBackOffFunction;
        return this;
    }

    public RpcClient<RESPONSE> build() {
        if (StringUtils.isBlank(serviceName)) {
            throw new IllegalStateException("RpcClientBuilder: Service name was not set");
        }
        if (StringUtils.isBlank(methodName)) {
            throw new IllegalStateException("RpcClientBuilder: Method name was not set");
        }
        LoadBalancerFactory lbFactory = injector.getInstance(LoadBalancerFactory.class);

        // one loadBalancer for each service
        LoadBalancer loadBalancer = lbFactory.getLoadBalancer(serviceName);
        return new RpcClient<>(loadBalancer, serviceName, methodName, retries, timeout,
                retryBackOffFunction, responseClass);
    }

    public void setResponseClass(Class<RESPONSE> responseClass) {
        this.responseClass = responseClass;
    }

    private void initialize() {
        ServiceProperties properties = injector.getInstance(ServiceProperties.class);
        retries = parseSetting(properties, "rpcClientRetries", DEFAULT_RETRIES);
        timeout = parseSetting(properties, "rpcClientTimeout", DEFAULT_RESPONSE_TIMEOUT);
    }

    private int parseSetting(ServiceProperties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (StringUtils.isNotBlank(value)) {
            try {
                int retval = Integer.parseInt(value);
                if (retval < 0) {
                    throw new IllegalArgumentException("Invalid " + key + " setting: " + value);
                }
                return retval;
            } catch (Exception ex) {
                logger.warn("Caught exception", ex);
            }
        }
        return defaultValue;
    }
}
