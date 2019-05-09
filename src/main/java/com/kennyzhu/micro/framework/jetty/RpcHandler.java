package com.kennyzhu.micro.framework.jetty;

import com.google.protobuf.Message;
import com.kennyzhu.micro.framework.*;
import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.google.common.collect.ImmutableSortedSet.of;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     RpcHandler   
 *  * @package    com.kennyzhu.micro.framework.jetty  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 16:24  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public abstract class RpcHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);

    protected final MethodHandlerDictionary handlers;
    protected final ServiceProperties serviceProps;

    //For now, we block services from getting certain input headers.
    //The reason is that these headers are also then used for outgoing requests.
    //If you need the incoming headers, we can create an additional bucket inside of OrangeContext to hold them.
    private static final Set<String> blackListedHeaders = of("user-agent", "content-length", "content-type",
            "date", "expect", "host");

    public RpcHandler(MethodHandlerDictionary handlers, ServiceProperties serviceProperties) {
        this.handlers = handlers;
        this.serviceProps = serviceProperties;
    }

    protected Map<String, String> gatherHttpHeaders(HttpServletRequest req) {
        Map<String, String> headers = new HashMap<>();

        Enumeration<String> headerNames = req.getHeaderNames();
        if (headerNames == null) {
            return headers;
        }

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = req.getHeaders(headerName);
            if (headerValues == null) {
                continue;
            }

            String headerNameLower = headerName.toLowerCase();
            if (! blackListedHeaders.contains(headerNameLower)) {
                if (headerValues.hasMoreElements()) {
                    headers.put(headerNameLower, headerValues.nextElement());
                }

                while (headerValues.hasMoreElements()) {
                    logger.debug("Duplicate http-header, discarding: {} = {}", headerName, headerValues.nextElement());
                }
            } else {
                logger.trace("Blocking header {}", headerNameLower);
            }
        }

        return headers;
    }

    /**
     * Invoke in the following order:
     * <ol><li>Global pre-handler hooks</li>
     * <li>Pre-handler hooks for this methodName</li>
     * <li>The handler</li>
     * <li>Post-handler hooks for this methodName</li>
     * <li>Global post-handler hooks</li></ol>
     */
    @SuppressWarnings("unchecked")
    protected Message invokeHandlerChain(String methodName, ServiceMethodHandler handler,
                                         Message request, OrangeContext context) throws RpcCallException {
        List<ServiceMethodPreHook<? extends Message>> preHooks = handlers.getPreHooksFor(methodName);
        for (ServiceMethodPreHook hook : preHooks) {
            request = hook.handleRequest(request, context);
        }
        Message response = handler.handleRequest(request, context);
        List<ServiceMethodPostHook<? extends Message>> postHooks = handlers.getPostHooksFor(methodName);
        for (ServiceMethodPostHook hook : postHooks) {
            response = hook.handleRequest(response, context);
        }
        return response;
    }

    protected void writeResponse(HttpServletResponse resp, int statusCode, String s) throws IOException {
        if ( statusCode != 200 ) {
            resp.setStatus(statusCode);
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
        }
        resp.getWriter().write(s);
        resp.getWriter().flush();
    }
}
