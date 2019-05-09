/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     RpcCallException   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 15:51  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.rpc.exception;

import com.google.gson.*;
import net.logstash.logback.marker.Markers;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;


public class RpcCallException extends Exception {
    private static final Logger logger = LoggerFactory.getLogger(RpcCallException.class);

    private static final String CATEGORY = "category";
    private static final String MESSAGE = "message";
    private static final String SOURCE = "source";
    private static final String CODE = "code";
    private static final String DATA = "data";
    private static final String RETRIABLE = "retriable";
    private static final String DETAIL = "detail";

    private static Map<Integer, RpcCallException.Category> cache = new HashMap<>();

    public enum Category {
        BadRequest(400, false),               //invalid params or malformed
        Unauthorized(401, false),             //not logged in
        InsufficientPermissions(403, false),  //not enough perms
        ResourceNotFound(404, false),         //resource not found
        Conflict(409, false),                 //resource conflict
        InternalServerError(500, true),       //unexpected exception
        BackendError(501, false),             //business logic failure
        RequestTimedOut(504, true);

        private int httpStatus;
        private boolean retriable; //default, can be overridden in the exception instance

        Category(int status, boolean retriable) {
            this.httpStatus = status;
            this.retriable = retriable;
            addToCache(status, this);
        }

        private void addToCache(int status, RpcCallException.Category category) {
            cache.put(status, category);
        }

        public int getHttpStatus() {
            return httpStatus;
        }

        public boolean isRetriable() {
            return retriable;
        }

        public static RpcCallException.Category fromStatus(int status) {
            return cache.get(status);
        }
    }

    private String source;
    private RpcCallException.Category category;
    private String errorCode;
    private String message;
    private String data;
    private boolean retriable;

    public RpcCallException(RpcCallException.Category category, String message) {
        super(); //builds stacktrace
        if (category == null) { throw new IllegalArgumentException("category is null"); }
        this.category = category;
        this.retriable = category.retriable;
        this.message = message;
    }

    public RpcCallException withSource(String source) {
        this.source = source;
        return this;
    }

    public RpcCallException withErrorCode(String errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public RpcCallException withData(String data) {
        this.data = data;
        return this;
    }

    public RpcCallException withRetriable(boolean retriable) {
        this.retriable = retriable;
        return this;
    }

    public String getSource() {
        return source;
    }

    public RpcCallException.Category getCategory() {
        return category;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getData() {
        return data;
    }

    public boolean isRetriable() {
        return retriable;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty(CATEGORY, category.getHttpStatus());
        obj.addProperty(MESSAGE, message);
        obj.addProperty(SOURCE, source);
        obj.addProperty(CODE, errorCode);
        obj.addProperty(DATA, data);
        obj.addProperty(RETRIABLE, retriable);
        return obj;
    }

    public static RpcCallException fromJson(String json) {
        Marker marker = Markers.append("payload", json);

        try {
            JsonParser parser = new JsonParser();
            JsonElement rawObject = parser.parse(json);
            if (rawObject instanceof JsonObject) {
                JsonObject object = (JsonObject) rawObject;
                RpcCallException.Category category = getCategory(object);
                String message = getMessage(object);
                RpcCallException retval = new RpcCallException(category, message);
                JsonElement element = object.get(SOURCE);
                if (element != null && !(element instanceof JsonNull)) {
                    retval.withSource(element.getAsString());
                }
                element = object.get(CODE);
                if (element != null && !(element instanceof JsonNull)) {
                    retval.withErrorCode(element.getAsString());
                }
                element = object.get(DATA);
                if (element != null && !(element instanceof JsonNull)) {
                    retval.withData(element.getAsString());
                }
                element = object.get(RETRIABLE);
                if (element != null && !(element instanceof JsonNull)) {
                    retval.withRetriable(element.getAsBoolean());
                }
                return retval;
            } else if (rawObject instanceof JsonPrimitive) {
                logger.warn(marker, "Expected an RpcCallException json object, but received: {}", rawObject.toString());
            }
        } catch (JsonParseException ex) {
            logger.warn(marker, "Expected an RpcCallException json object, but received: {}", json);
        } catch (Exception ex) {
            logger.warn(marker, "Caught exception parsing RpcCallException: " + json, ex);
        }
        return null;
    }

    private static String getMessage(JsonObject object) {
        String message = StringUtils.EMPTY;
        if (object.has(MESSAGE)) {
            message = object.get(MESSAGE).getAsString();
        } else if (object.has(DETAIL)) {
            message = object.get(DETAIL).getAsString();
        }
        return message;
    }

    /**
     * provides a {@link RpcCallException.Category} by the given {@link JsonObject}.
     *
     * @param object JSON element
     * @return Category that is provided in JSON, if the code is not known it will fall back to {@link RpcCallException.Category#InternalServerError}.
     */
    private static Category getCategory(JsonObject object) {
        RpcCallException.Category category = RpcCallException.Category.InternalServerError;
        if (object.has(CATEGORY) && object.get(CATEGORY).isJsonPrimitive()) { // category could be an object ...
            JsonPrimitive primitive = object.getAsJsonPrimitive(CATEGORY);
            try {
                category = RpcCallException.Category.fromStatus(primitive.getAsInt());
            } catch (NumberFormatException nfe) {
                category = RpcCallException.Category.InternalServerError;
            }
        } else if (object.has(CODE)) {
            category = RpcCallException.Category.fromStatus(object.get(CODE).getAsInt());
        }
        return category != null ? category : RpcCallException.Category.InternalServerError;
    }
}
