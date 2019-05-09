package com.kennyzhu.micro.module;

import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import com.kennyzhu.micro.framework.rpc.LoadBalancer;
import com.kennyzhu.micro.framework.rpc.LoadBalancerImpl;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ClientInjectionModule   
 *  * @package    com.kennyzhu.micro.module  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/9 11:45  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public class ClientInjectionModule extends BaseApiClientInectionModule {
    public ClientInjectionModule(String serviceName, ServiceProperties props) {
        super(serviceName, props);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(ServiceProperties.class).toInstance(serviceProperties);
        bind(LoadBalancer.class).to(LoadBalancerImpl.class);
        bind(JsonHttpClientService.class).to(JsonHttpClientService.class);
    }

}
