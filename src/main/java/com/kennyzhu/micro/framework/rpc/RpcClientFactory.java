package com.kennyzhu.micro.framework.rpc;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     RpcClientFactory   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description  如果是json client，默认Message构造即可.
 *  * @author kennyzhu     
 *  * @date   2019/5/8 11:54  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.protobuf.Message;

@Singleton
public class RpcClientFactory {
    protected Injector injector;

    @Inject
    public RpcClientFactory(Injector injector) {
        this.injector = injector;
    }

    /**
     * Build an RpcClientBuilder.  Use the withXXX methods to customize the behavior
     * of the client, then finish with builder.build().
     */
    public <RESPONSE extends Message> RpcClientBuilder<RESPONSE> newClient(String serviceName, String methodName,
                                                                           Class<RESPONSE> responseClass) {
        @SuppressWarnings("unchecked")
        RpcClientBuilder<RESPONSE> retval = (RpcClientBuilder<RESPONSE>) injector.getInstance(RpcClientBuilder.class);
        retval.setServiceName(serviceName);
        retval.setMethodName(methodName);
        retval.setResponseClass(responseClass);
        return retval;
    }
}
