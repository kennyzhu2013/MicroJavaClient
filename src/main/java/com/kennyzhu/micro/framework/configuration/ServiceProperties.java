package com.kennyzhu.micro.framework.configuration;

// import com.jcabi.manifests.Manifests;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ServiceProperties   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/6 15:36  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public class ServiceProperties {
    // change if modified by caller.
    public static Logger logger = LoggerFactory.getLogger(ServiceProperties.class);

    // for client
    public static final String REGISTRY_SERVER_KEY = "registryServer";

    public static final String LOG_LEVEL_KEY = "logLevel";
    public static final String SERVICE_UNKNOWN = "com.sixt.service.unknown";

    private String serviceName = SERVICE_UNKNOWN;
    private String serviceVersion = "0.0.0-fffffff";

   // properties
   private Map<String, String> allProperties = new HashMap<>();

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    /**
     * Consul or etcd server in the form of host:port
     */
    public String getRegistryServer() {
        return allProperties.get(REGISTRY_SERVER_KEY);
    }

    public void addProperty(String key, String value) {
        allProperties.put(key, value);
    }

    public String getProperty(String key) {
        return allProperties.get(key);
    }

    public Map<String, String> getAllProperties() {
        return allProperties;
    }

    public int getIntegerProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (StringUtils.isNotBlank(value)) {
            try {
                return Integer.valueOf(value);
            } catch (Exception ex) {
                logger.info("Service property '{}' was not an integer value: '{}'", key, value);
            }
        }
        return defaultValue;
    }

    //Intended only for testing
    public void setServiceName(String name) {
        serviceName = name;
    }

    public void setServiceVersion(String version) {
        serviceVersion = version;
    }
}
