/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     LoadBalancerFactory   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description  
 *  * @author kennyzhu     
 *  * @date   2019/5/6 16:53  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.rpc;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class LoadBalancerFactory {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerFactory.class);

    protected Injector injector;
    // to add initialize : protected ServiceDiscoveryProvider provider;
    protected Map<String, LoadBalancer> loadBalancers = new HashMap<>();

    @Inject
    public LoadBalancerFactory(Injector injector) {
        this.injector = injector;
    }

    // one for each
    public synchronized LoadBalancer getLoadBalancer(String serviceName) {
        LoadBalancer retval = loadBalancers.get(serviceName);
        if (retval == null) {
            retval = buildLoadBalancer(serviceName);
        }
        return retval;
    }

    private LoadBalancer buildLoadBalancer(String svc) {
        LoadBalancer retval = injector.getInstance(LoadBalancer.class);
        logger.debug("Returning instance of {} for the LoadBalancer instance", retval.getClass().getSimpleName());
        retval.setServiceName(svc);
        loadBalancers.put(svc, retval);

        return retval;
    }

    //intended only for debugging
    public Set<String> getServices() {
        return loadBalancers.keySet();
    }

    public void shutdown() {
        // else to do?
    }
}
