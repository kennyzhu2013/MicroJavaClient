/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     OrangeContext   
 *  * @package    com.kennyzhu.micro.framework  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 15:23  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OrangeContext {
    public final static String CORRELATION_ID = "correlation-id";
    private static final String RPC_MEDIA_SERVER = "X-Media-Server";

    private String correlationId;
    private Map<String, String> properties = new HashMap<>();
    public OrangeContext() {
        this(null, null);
    }

    public OrangeContext(Map<String, String> props) {
        this(props == null ? null : props.get("x-correlation-id"), props);
    }

    public OrangeContext(String correlationId, Map<String, String> props) {
        if (correlationId == null) {
            this.correlationId = UUID.randomUUID().toString();
        } else {
            this.correlationId = correlationId;
        }
        if (props != null) {
            this.properties = props;
        }
    }

    public OrangeContext(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        if (correlationId != null && this.correlationId == null) {
            this.correlationId = correlationId;
        }
    }

    public String getRpcMediaServer() {
        return getProperty(RPC_MEDIA_SERVER);
    }
    public void setProperty(String key, String value) {
        properties.put(key.toLowerCase(), value);
    }

    public Map<String, String> getProperties() {
        return new HashMap<>(properties);
    }
    public String getProperty(String key) {
        return properties.get(key.toLowerCase());
    }

    //TODO: getIntProperty, getLongProperty, etc.
}
