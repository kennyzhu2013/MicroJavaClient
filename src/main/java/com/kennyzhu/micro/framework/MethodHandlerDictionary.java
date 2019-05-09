/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     MethodHandlerDictionary   
 *  * @package    com.kennyzhu.micro.framework  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 16:34  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework;

import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MethodHandlerDictionary {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandlerDictionary.class);

    public static final String GLOBAL = "*";

    private Map<String, ServiceMethodHandler<? extends Message, ? extends Message>> methodHandlers;
    private List<ServiceMethodPreHook<? extends Message>> globalPreHooks;
    private List<ServiceMethodPostHook<? extends Message>> globalPostHooks;
    private Map<String, List<ServiceMethodPreHook<? extends Message>>> methodPreHooks;
    private Map<String, List<ServiceMethodPostHook<? extends Message>>> methodPostHooks;

    public MethodHandlerDictionary() {
        this.methodHandlers = new HashMap<>();
        this.globalPreHooks = new ArrayList<>();
        this.globalPostHooks = new ArrayList<>();
        this.methodPreHooks = new HashMap<>();
        this.methodPostHooks = new HashMap<>();
    }

    /**
     * Add a hook to inspect or modify incoming requests.  Invoked in the same order as configured at runtime
     * @param endpoint Use MethodHandlerDictionary.GLOBAL to apply to all handlers, or the RPC method full-name
     * @param handlerClass ServiceMethodPreHook instance
     */
    public void addPreHook(String endpoint, ServiceMethodPreHook<? extends Message> handlerClass) {
        if (GLOBAL.equals(endpoint)) {
            globalPreHooks.add(handlerClass);
        } else {
            List<ServiceMethodPreHook<? extends Message>> hooks = methodPreHooks.get(endpoint);
            if (hooks == null) {
                hooks = new ArrayList<>();
                methodPreHooks.put(endpoint, hooks);
            }
            hooks.add(handlerClass);
        }
    }

    /**
     * Add a hook to inspect or modify outgoing responses.  Invoked in the same order as configured at runtime
     * @param endpoint Use MethodHandlerDictionary.GLOBAL to apply to all handlers, or the RPC method full-name
     * @param handlerClass ServiceMethodPostHook instance
     */
    public void addPostHook(String endpoint, ServiceMethodPostHook<? extends Message> handlerClass) {
        if (GLOBAL.equals(endpoint)) {
            globalPostHooks.add(handlerClass);
        } else {
            List<ServiceMethodPostHook<? extends Message>> hooks = methodPostHooks.get(endpoint);
            if (hooks == null) {
                hooks = new ArrayList<>();
                methodPostHooks.put(endpoint, hooks);
            }
            hooks.add(handlerClass);
        }
    }

    public void put(String endpoint, ServiceMethodHandler<? extends Message, ? extends Message> instance) {
        ServiceMethodHandler existing = methodHandlers.get(endpoint);
        if (existing != null) {
            logger.warn("Overwriting ServiceMethodHandler registration for {}", endpoint);
        }
        methodHandlers.put(endpoint, instance);
    }

    public Map<String, ServiceMethodHandler<? extends Message, ? extends Message>> getMethodHandlers() {
        return methodHandlers;
    }

    public ServiceMethodHandler<? extends Message, ? extends Message> getMethodHandler(String endpoint) {
        return methodHandlers.get(endpoint);
    }

    /**
     * For backward-compatibility
     */
    @Deprecated
    public ServiceMethodHandler<? extends Message, ? extends Message> get(String endpoint) {
        return getMethodHandler(endpoint);
    }

    public List<ServiceMethodPreHook<? extends Message>> getPreHooksFor(String methodName) {
        List<ServiceMethodPreHook<? extends Message>> retval = new ArrayList<>(globalPreHooks);
        List<ServiceMethodPreHook<? extends Message>> methodHooks = methodPreHooks.get(methodName);
        if (methodHooks != null) {
            retval.addAll(methodHooks);
        }
        return Collections.unmodifiableList(retval);
    }

    public List<ServiceMethodPostHook<? extends Message>> getPostHooksFor(String methodName) {
        List<ServiceMethodPostHook<? extends Message>> hooks = methodPostHooks.get(methodName);
        List<ServiceMethodPostHook<? extends Message>> retval = new ArrayList<>();
        if (hooks != null) {
            retval.addAll(hooks);
        }
        if (! globalPostHooks.isEmpty()) {
            retval.addAll(globalPostHooks);
        }
        return Collections.unmodifiableList(retval);
    }

    public boolean hasMethodHandler(String method) {
        return methodHandlers.containsKey(method);
    }
}
