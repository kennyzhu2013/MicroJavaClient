package com.kennyzhu.micro.framework.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.servlet.http.HttpServletResponse;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     JsonRpcResponse   
 *  * @package    com.kennyzhu.micro.framework.json  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/8 11:32  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public class JsonRpcResponse {
    public final static String ID_FIELD = "id";
    public final static String ERROR_FIELD = "error";
    public final static String RESULT_FIELD = "result";

    private String id;
    private String result;
    private String error;
    // HTTP status code, not part of the JSON-RPC spec, used for internal purposes only
    private int statusCode;

    public JsonRpcResponse(String id, String result, String error, int statusCode) {
        setId(id);
        setResult(result);
        setError(error);
        setStatusCode(statusCode);
    }

    public JsonObject toJson() {
        JsonObject retval = new JsonObject();
        retval.addProperty(ID_FIELD, id);
        retval.addProperty(ERROR_FIELD, error);
        retval.addProperty(RESULT_FIELD, result);
        return retval;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append(ID_FIELD, getId());
        builder.append(RESULT_FIELD, getResult());
        builder.append(ERROR_FIELD, getError());
        return builder.toString();
    }

    public static JsonRpcResponse fromString(String rawResponse) {
        JsonParser parser = new JsonParser();
        JsonObject response = parser.parse(rawResponse).getAsJsonObject();
        JsonElement id = response.get("id");
        JsonElement errorElement = response.get("error");
        int responseStatus = HttpServletResponse.SC_OK;
        String error;
        if (! (errorElement instanceof JsonNull)) {
            if (errorElement instanceof JsonObject) {
                error = errorElement.toString();
                // try parsing it into RpcCallException to get the HttpStatus from there
                RpcCallException rpcEx = RpcCallException.fromJson(error);
                if (rpcEx != null) {
                    responseStatus = rpcEx.getCategory().getHttpStatus();
                    JsonElement resultElement = response.get("result");
                    return new JsonRpcResponse(id.getAsString(), resultElement == null ? "" : resultElement.getAsString(),
                            errorElement.getAsString(), responseStatus);
                }
            }
            error = errorElement.getAsString();
            if (StringUtils.isNotBlank(error)) {
                responseStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
        }

        JsonElement resultElement = response.get("result");
        return new JsonRpcResponse(id.getAsString(), resultElement == null ? "" : resultElement.getAsString(),
                errorElement.getAsString(), responseStatus);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public int getStatusCode() { return statusCode; }

    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
}
