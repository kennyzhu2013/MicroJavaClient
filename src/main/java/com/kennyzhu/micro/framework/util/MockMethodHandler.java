package com.kennyzhu.micro.framework.util;

import com.google.protobuf.Message;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     MockMethodHandler   
 *  * @package    com.kennyzhu.micro.framework.util  
 *  * @description  为反射获取请求和响应类型设置.. 
 *  * @author kennyzhu     
 *  * @date   2019/5/8 11:43  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public interface MockMethodHandler<REQ extends Message, RES extends Message> {
    Class<REQ> getRequestType();

    Class<RES> getResponseType();

    int getMethodCallCounter();

    void resetMethodCallCounter();
}
