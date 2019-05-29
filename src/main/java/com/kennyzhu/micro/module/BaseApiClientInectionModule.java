package com.kennyzhu.micro.module;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.kennyzhu.micro.framework.MethodHandlerDictionary;
import com.kennyzhu.micro.framework.OrangeContext;
import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import com.kennyzhu.micro.framework.rpc.HttpClientUrlWrapper;
import com.kennyzhu.micro.framework.rpc.HttpClientWrapper;
import com.kennyzhu.micro.framework.rpc.LoadBalancer;
import com.kennyzhu.micro.framework.rpc.LoadBalancerImpl;
import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     BaseClientInectionModule   
 *  * @package    com.kennyzhu.micro.module  
 *  * @description  此类用于api形式的服务调用
 *  * @author kennyzhu     
 *  * @date   2019/5/8 14:52  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public class BaseApiClientInectionModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(BaseApiClientInectionModule.class);

    protected final ServiceProperties serviceProperties;
    protected final MethodHandlerDictionary methodHandlerDictionary = new MethodHandlerDictionary();


    @Override
    protected void configure() {
        bind(HttpClientWrapper.class).to(HttpClientUrlWrapper.class);
        bind(OrangeContext.class);
    }

    protected Injector injector;

    public BaseApiClientInectionModule(String serviceName, ServiceProperties props) {
        this.injector = injector;
        this.serviceProperties = props;
        serviceProperties.setServiceName(serviceName);
        if (props.getProperty("registry") == null) {
            serviceProperties.addProperty("registry", "consul");
        }
        if (props.getProperty("registryServer") == null) {
            serviceProperties.addProperty("registryServer", "localhost:8500");
        }
    }

    @Provides
    public MethodHandlerDictionary getMethodHandlers() {
        return methodHandlerDictionary;
    }

    @Provides
    public HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        client.setFollowRedirects(false);
        client.setMaxConnectionsPerDestination(32);
        client.setConnectTimeout(100);
        client.setAddressResolutionTimeout(100);
        //You can set more restrictive timeouts per request, but not less, so
        //  we set the maximum timeout of 1 hour here.
        client.setIdleTimeout(60 * 60 * 1000);
        try {
            client.start();
        } catch (Exception e) {
            logger.error("Error building http client", e);
        }
        return client;
    }

    @Provides
    public ExecutorService getExecutorService() {
        return Executors.newCachedThreadPool();
    }

    public ServiceProperties getServiceProperties() {
        return serviceProperties;
    }
}
