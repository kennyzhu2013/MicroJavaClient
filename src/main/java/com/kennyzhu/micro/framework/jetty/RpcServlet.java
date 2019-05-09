package com.kennyzhu.micro.framework.jetty;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     RpcServlet   
 *  * @package    com.kennyzhu.micro.framework.jetty  
 *  * @description  暂时不需实现，只有server模式才要用到
 *  * @author kennyzhu     
 *  * @date   2019/5/7 16:22  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */

@Singleton
public class RpcServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RpcServlet.class);

    public final static String CONTENT_TYPE = "Content-Type";
    public final static String TYPE_JSON = "application/json";
    public final static String TYPE_PROTO = "application/proto";
    public final static String TYPE_OCTET = "application/octet-stream";

    // protected JsonHandler jsonRpcHandler;
    // protected ProtobufHandler protobufHandler;
    @Inject
    public RpcServlet() {
    }

    private boolean isProtobuf(String ctype) {
        if (ctype == null) {
            return false;
        }
        return ctype.startsWith(TYPE_PROTO) || ctype.startsWith(TYPE_OCTET);
    }

    private boolean isJson(String ctype) {
        if (ctype == null) {
            return false;
        }
        return ctype.startsWith(TYPE_JSON);
    }
}
