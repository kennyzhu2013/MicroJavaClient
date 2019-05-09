package com.kennyzhu.micro.framework.rpc;


import java.util.List;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     LoadBalancer   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description  client load balance 
 *  * @author kennyzhu     
 *  * @date   2019/5/6 15:57  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public interface LoadBalancer {
    /**
     * There is one LoadBalancer per service.  Set the service name for this instance.
     */
    void setServiceName(String serviceName);

    String getServiceName();

    /**
     * Used by the RpcClient to get a wrapper that directs calls to a specific instance
     * of a called service based on health.
     */
    HttpClientWrapper getHttpClientWrapper();

    /**
     * Get an instance of a called service that is likely to be healthy (more specifically:
     * an instance that is not known to not be healthy, unless it is a probe to allow the instance
     * to be healthy again.)
     */
    ServiceEndpoint getHealthyInstance();

    /**
     * Like above, but excluding ServiceEndpoints that the client has already tried.
     * Influenced by FeatureFlags.shouldDisableRpcInstanceRetry
     */
    ServiceEndpoint getHealthyInstanceExclude(List<ServiceEndpoint> triedEndpoints);

    /**
     * Pause the calling thread until an apparently healthy instance appears through
     * service discovery.
     */
    void waitForServiceInstance();

    /**
     * Used by the service registry watcher to let the LoadBalancer know about changes to
     * service instances based on registry information.
     */
    void updateServiceEndpoints(LoadBalancerUpdate updates);
}
