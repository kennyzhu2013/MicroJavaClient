/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ServiceEndpoint   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO} :must add health check. 
 *  * @author kennyzhu     
 *  * @date   2019/5/6 16:01  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.rpc;


import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceEndpoint {
    protected String availZone;
    protected String hostAndPort;
    protected CircuitBreakerState circuitBreaker;
    protected AtomicInteger servingRequests = new AtomicInteger(0); //intended only for probe logic
    protected String serviceName;

    public ServiceEndpoint(ScheduledThreadPoolExecutor executor,
                           String hostAndPort, String availZone) { //}, dependencyHealthCheck) {
        this( hostAndPort, availZone, new CircuitBreakerState(executor) );
        // dependencyHealthCheck.monitorServiceEndpoint(this);
    }

    public ServiceEndpoint(String hostAndPort, String availZone, CircuitBreakerState cb) {
        this.hostAndPort = hostAndPort;
        this.availZone = availZone == null ? "" : availZone;
        this.circuitBreaker = cb;
    }

    public String getHostAndPort() {
        return hostAndPort;
    }

    public String getAvailZone() {
        return availZone;
    }

    public void setCircuitBreakerState(CircuitBreakerState.State state) {
        this.circuitBreaker.setState(state);
    }

    public CircuitBreakerState.State getCircuitBreakerState() {
        return circuitBreaker.getState();
    }

    public boolean canServeRequests() {
        return circuitBreaker.canServeRequests(servingRequests.get() > 0);
    }

    public void incrementServingRequests() {
        servingRequests.incrementAndGet();
    }

    public void requestComplete(boolean success) {
        servingRequests.decrementAndGet();
        circuitBreaker.requestComplete(success);
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceEndpoint that = (ServiceEndpoint)  o;

        if (availZone != null ? !availZone.equals(that.availZone) : that.availZone != null) return false;
        return hostAndPort != null ? hostAndPort.equals(that.hostAndPort) : that.hostAndPort == null;
    }

    @Override
    public int hashCode() {
        int result = availZone != null ? availZone.hashCode() : 0;
        result = 31 * result + (hostAndPort != null ? hostAndPort.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return availZone + "/" + hostAndPort;
    }
}
