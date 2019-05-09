package com.kennyzhu.micro.framework.rpc;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     JsonRpcCallExceptionDecoder   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 17:02  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallExceptionDecoder;
import org.eclipse.jetty.client.api.ContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonRpcCallExceptionDecoder implements RpcCallExceptionDecoder {
    private static final Logger logger = LoggerFactory.getLogger(JsonRpcCallExceptionDecoder.class);

    @Override
    public RpcCallException decodeException(ContentResponse response) throws RpcCallException {
        try {
            if (response != null) {
                JsonObject json = (JsonObject) new JsonParser().parse(response.getContentAsString());

                // some error string define here.
                JsonElement error = json.get("error");
                if (error != null) {
                    return RpcCallException.fromJson(error.toString());
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
