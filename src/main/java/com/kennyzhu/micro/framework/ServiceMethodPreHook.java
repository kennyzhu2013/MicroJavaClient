package com.kennyzhu.micro.framework;

import com.google.protobuf.Message;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ServiceMethodPreHook   
 *  * @package    com.kennyzhu.micro.framework  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 16:31  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public interface ServiceMethodPreHook <REQ extends Message> {
    /**
     * Perform an action and/or modify an incoming request
     * @param request The request being made
     * @param ctx The OrangeContext for the request
     * @return Either the incoming request, or a new request
     * @throws RpcCallException
     */
    REQ handleRequest(REQ request, OrangeContext ctx) throws RpcCallException;
}
