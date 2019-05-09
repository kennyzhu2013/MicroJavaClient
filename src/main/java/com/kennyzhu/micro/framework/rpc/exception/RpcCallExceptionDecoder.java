/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     RpcCallExceptionDecoder   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    decode rpc method
 *  * @author kennyzhu     
 *  * @date   2019/5/7 16:06  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.rpc.exception;

import org.eclipse.jetty.client.api.ContentResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public interface RpcCallExceptionDecoder {
   RpcCallException decodeException(ContentResponse response) throws RpcCallException;

    static String exceptionToString(Throwable ex) {
        if (ex == null) {
            return "null";
        }
        StringWriter str = new StringWriter();
        str.append("Exception: ").append(ex.getClass().getSimpleName());
        str.append(" Message: ").append(ex.getMessage());
        str.append(" Stacktrace: ");
        Throwable cause = ex.getCause();
        if (cause != null) {
            str.append("\nCause: ").append(exceptionToString(cause));
        }
        PrintWriter writer = new PrintWriter(str);
        try {
            ex.printStackTrace(writer);
            return str.getBuffer().toString();
        } finally {
            try {
                str.close();
                writer.close();
            } catch (IOException e) {}
        }
    }
}
