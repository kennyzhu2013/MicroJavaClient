/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     JsonHandler   
 *  * @package    com.kennyzhu.micro.framework.jetty  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 16:24  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.jetty;

import com.google.common.io.CharStreams;
import com.google.gson.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;

import com.kennyzhu.micro.framework.*;
import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import com.kennyzhu.micro.framework.json.JsonRpcRequest;
import com.kennyzhu.micro.framework.json.JsonRpcResponse;
import com.kennyzhu.micro.framework.protobuf.ProtobufUtil;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static com.kennyzhu.micro.framework.OrangeContext.CORRELATION_ID;
import static com.kennyzhu.micro.framework.jetty.RpcServlet.TYPE_JSON;
import static com.kennyzhu.micro.framework.json.JsonRpcRequest.METHOD_FIELD;
import static com.kennyzhu.micro.framework.json.JsonRpcRequest.PARAMS_FIELD;
import static com.kennyzhu.micro.framework.util.ReflectionUtil.findSubClassParameterType;

@Singleton
public class JsonHandler extends RpcHandler {
    private static final Logger logger = LoggerFactory.getLogger(JsonHandler.class);

    @Inject
    public JsonHandler(MethodHandlerDictionary handlers, ServiceProperties serviceProperties) {
        super(handlers, serviceProperties);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        logger.debug("Handling json request");

        long startTime = System.nanoTime();
        Map<String, String> headers = gatherHttpHeaders(req);
        OrangeContext context = new OrangeContext(headers);
        try {
            // use MDC for call params.
            MDC.put(CORRELATION_ID, context.getCorrelationId());

            String postedContent = CharStreams.toString(req.getReader());
            logger.debug("Request JSON: {}", postedContent);

            JsonRpcRequest rpcRequest;
            try {
                rpcRequest = parseRpcRequest(postedContent);
            } catch (IllegalArgumentException iaex) {
                logger.warn("Error parsing request: " + postedContent, iaex);
                @SuppressWarnings("ThrowableNotThrown") RpcCallException callException = new RpcCallException(RpcCallException.Category.BadRequest,
                        iaex.getMessage());
                JsonObject jsonResponse = new JsonObject();

                // todo : define error json and JsonRpcResponse.
                jsonResponse.add("error", callException.toJson());
                writeResponse(resp, HttpServletResponse.SC_BAD_REQUEST, jsonResponse.toString());

                // fail count.
                return;
            }

            // set correlation id for back
            context.setCorrelationId(rpcRequest.getId());
            JsonRpcResponse finalResponse = dispatchJsonRpcRequest(rpcRequest, context);

            resp.setContentType(TYPE_JSON);
            writeResponse(resp, finalResponse.getStatusCode(), finalResponse.toJson().toString());

            //TODO: should we check the response for errors (for metrics)?
        } catch (IOException e) {
            logger.error("Error handling request", e);
        } finally {
            MDC.remove(CORRELATION_ID);
        }
    }

    // support NIO call back.
    protected JsonRpcRequest parseRpcRequest(String jsonRequestString)
            throws IllegalArgumentException {
        JsonObject jsonRpcRequest = null;
        try {
            jsonRpcRequest = new JsonParser().parse(jsonRequestString).getAsJsonObject();
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        String methodNamet = jsonRpcRequest.get(JsonRpcRequest.METHOD_FIELD).getAsString();
        if (methodNamet == null || methodNamet.isEmpty()) {
            throw new IllegalArgumentException("Missing method name");
        }
        if (! handlers.hasMethodHandler(methodNamet)) {
            throw new IllegalArgumentException("No handler registered for method '" +
                    methodNamet + "'");
        }

        String paramsArray = jsonRpcRequest.get(PARAMS_FIELD).getAsString();
        String idElement = jsonRpcRequest.get(JsonRpcRequest.ID_FIELD).getAsString();
        return new JsonRpcRequest(idElement, methodNamet, paramsArray);
    }

    @SuppressWarnings("unchecked")
    private JsonRpcResponse dispatchJsonRpcRequest(JsonRpcRequest rpcRequest, OrangeContext cxt) {
        JsonRpcResponse jsonResponse = new JsonRpcResponse(rpcRequest.getId(), "",
                "", HttpServletResponse.SC_OK);
        try {
            ServiceMethodHandler handler = handlers.getMethodHandler(rpcRequest.getMethod());
            Message innerRequest = convertJsonToProtobuf(handler, rpcRequest);
            String idElement = rpcRequest.getId();
            if (idElement == "") {
                jsonResponse.setId("-1");
            }
            Message innerResponse = invokeHandlerChain(rpcRequest.getMethod(), handler, innerRequest, cxt);
            jsonResponse.setResult(ProtobufUtil.protobufToJson(innerResponse).getAsString());
        } catch (RpcCallException rpcEx) {
            logger.debug("Error processing request", rpcEx);
            jsonResponse.setError(rpcEx.toString());
            jsonResponse.setStatusCode(rpcEx.getCategory().getHttpStatus());
        } catch (Exception ex) {
            logger.warn("Error processing request", ex);
            if (ex.getMessage() != null) {
                jsonResponse.setError(ex.getMessage());
            }
            jsonResponse.setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        return jsonResponse;
    }

    @SuppressWarnings("unchecked")
    private Message convertJsonToProtobuf(ServiceMethodHandler handler,
                                          JsonRpcRequest rpcRequest) throws RpcCallException {
        try {
            Class<?> requestKlass = findSubClassParameterType(handler, 0);
            return ProtobufUtil.jsonToProtobuf(rpcRequest.getParams(),
                    (Class<? extends Message>) requestKlass);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Reflection for handler " +
                    handler.getClass() + " failed");
        } catch (RuntimeException ex) {
            throw new RpcCallException(RpcCallException.Category.BadRequest, "Invalid request");
        }
    }
}
