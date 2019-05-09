package com.kennyzhu.micro.framework;

import com.google.protobuf.Message;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ServiceMethodPostHook   
 *  * @package    com.kennyzhu.micro.framework  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 16:32  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public interface ServiceMethodPostHook <RES extends Message>{
    /**
     * Perform an action and/or modify an outgoing response
     * @param response The response being returned from the ServiceMethodHandler
     * @param ctx The OrangeContext for the request
     * @return Either the outgoing response, or a new response
     * @throws RpcCallException
     */
    RES handleRequest(RES response, OrangeContext ctx) throws RpcCallException;
}
