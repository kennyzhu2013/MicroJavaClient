package com.kennyzhu.micro.framework.json;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     JsonRpcRequest   
 *  * @package    com.kennyzhu.micro.framework.json  
 *  * @description 建议json结构。
 *  * @author kennyzhu     
 *  * @date   2019/5/7 16:44  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public class JsonRpcRequest {
    // suggest struct, params are body.
    public final static String ID_FIELD = "id";
    public final static String METHOD_FIELD = "method";
    public final static String PARAMS_FIELD = "params";

    // id json.
    private String id;
    // method not need..
    private String method;
    private String params;

    public JsonRpcRequest(String id, String method, String params) {
        setId(id);
        setMethod(method);
        setParams(params);
    }

    // simply return the json body.
    @Override
    public String toString() {
        JsonObject json = new JsonObject();
        json.addProperty(ID_FIELD, id);
        json.addProperty(METHOD_FIELD, method);
        json.addProperty(PARAMS_FIELD, params);
        return json.toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}
