/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ProtobufRpcCallExceptionDecoder   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/8 11:05  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.rpc.exception;

import com.kennyzhu.micro.framework.protobuf.ProtobufRpcResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtobufRpcCallExceptionDecoder implements RpcCallExceptionDecoder {
    private static final Logger logger = LoggerFactory.getLogger(ProtobufRpcCallExceptionDecoder.class);

    @Override
    public RpcCallException decodeException(ContentResponse response) throws RpcCallException {
        try {
            if (response != null) {
                byte[] data = response.getContent();
                if (ArrayUtils.isEmpty(data)) {
                    logger.warn("Unable to decode: empty response received");
                    return new RpcCallException(RpcCallException.Category.InternalServerError,
                            "Empty response received");
                }
                ProtobufRpcResponse pbResponse = new ProtobufRpcResponse(data);
                String error = pbResponse.getErrorMessage();
                if (error != null) {
                    return RpcCallException.fromJson(error);
                }
            }
        } catch (Exception ex) {
            logger.warn("Caught exception decoding protobuf response exception", ex);
            throw new RpcCallException(RpcCallException.Category.InternalServerError,
                    RpcCallExceptionDecoder.exceptionToString(ex));
        }
        return null;
    }
}
