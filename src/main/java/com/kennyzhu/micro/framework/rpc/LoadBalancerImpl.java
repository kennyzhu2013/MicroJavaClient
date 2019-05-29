/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     LoadBalancerImpl   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/6 17:13  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.rpc;

import com.google.inject.Inject;
import com.sun.corba.se.spi.ior.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static net.logstash.logback.marker.Markers.append;


public class LoadBalancerImpl implements LoadBalancer {
    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerImpl.class);

    protected ServiceProperties serviceProps;
    protected HttpClientWrapper httpClientWrapper;
    protected String serviceName;
    //we don't expect more than 3, so hashmap doesn't necessarily make sense
    protected List<AvailabilityZone> availabilityZones = new ArrayList<>();
    protected ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();

    // if no endpoints added when using registry, ingore the two vars when using f5 for lb.
    protected Semaphore notificationSemaphore = new Semaphore(0);
    protected AtomicBoolean haveEndpoints = new AtomicBoolean(false);

    @Inject
    public LoadBalancerImpl(ServiceProperties serviceProps,
                            HttpClientWrapper wrapper) {
        this.serviceProps = serviceProps;
        this.httpClientWrapper = wrapper;
        httpClientWrapper.setLoadBalancer(this);
    }

    @Override
    public HttpClientWrapper getHttpClientWrapper() {
        return httpClientWrapper;
    }

    @Override
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public void updateServiceEndpoints(LoadBalancerUpdate updates) {
        mutex.writeLock().lock();
        try {
            Marker logMarker = append("serviceName", this.serviceName);
            for (ServiceEndpoint ep : updates.getNewServices()) {
                logger.debug(logMarker,
                        "Endpoint for {} became available: {}", this.serviceName, ep.getHostAndPort());
                addServiceEndpoint(ep);
            }
            for (ServiceEndpoint ep : updates.getDeletedServices()) {
                logger.debug(logMarker,
                        "Endpoint for {} became unavailable: {}", this.serviceName, ep.getHostAndPort());
                updateEndpointHealth(ep, CircuitBreakerState.State.UNHEALTHY);
            }
            for (ServiceEndpoint ep : updates.getUpdatedServices()) {
                logger.debug(logMarker,
                        "Health of endpoint {} of {} changed to {}", ep.getHostAndPort(), this.serviceName,
                        ep.getCircuitBreakerState());
                updateEndpointHealth(ep, ep.getCircuitBreakerState());
            }
        } finally {
            mutex.writeLock().unlock();
        }
    }

    /**
     * We rely on the object that populates us to order availability zones
     * with primary first, then going nearest to furthest. (implying priority)
     * Only to be used from this class or tests
     */
    void addServiceEndpoint(ServiceEndpoint endpoint) {
        boolean found = false;
        for (AvailabilityZone az : availabilityZones) {
            if (az.getName().equals(endpoint.getAvailZone())) {
                az.addServiceEndpoint(endpoint);
                found = true;
            }
        }
        if (! found) {
            AvailabilityZone az = new AvailabilityZone();
            az.addServiceEndpoint(endpoint);
            availabilityZones.add(az);
            logger.debug("addServiceEndpoint: availZone added {} ", az);
        }
        haveEndpoints.set(true);
        notificationSemaphore.release();
    }

    protected void updateEndpointHealth(ServiceEndpoint ep, CircuitBreakerState.State state) {
        for (AvailabilityZone az : availabilityZones) {
            if (az.getName().equals(ep.getAvailZone())) {
                az.updateEndpointHealth(ep, state);
                logger.error("updateEndpointHealth: availZone updated {} ", availabilityZones);
                return;
            }
        }
        logger.error("updateEndpointHealth: availZone {} not found", ep.getAvailZone());
    }

    protected int getAvailabilityZoneCount() {
        return availabilityZones.size();
    }

    /**
     * Try to find an endpoint in our primary AZ.  If none found, try further AZs.
     * Modifies state
     */
    @Override
    public ServiceEndpoint getHealthyInstance() {
        // logger.info("getHealthyInstance 00000");
        if (! haveEndpoints.get()) {
            //wait for the first one to come in.
            try {
                notificationSemaphore.tryAcquire(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }
        mutex.readLock().lock();
        logger.info("getHealthyInstance zones : {}, objectId : {}",
                availabilityZones, this.hashCode());
        try {
            for (AvailabilityZone az : availabilityZones) {
                ServiceEndpoint next = az.nextEndpoint();
                if (next != null) {
                    logger.debug("Returning instance {} for {}", next.getHostAndPort(), serviceName);
                    return next;
                }
            }
            logger.debug("Returning instance null");
            return null;
        } finally {
            mutex.readLock().unlock();
        }
    }

    //modifies state
    @Override
    public ServiceEndpoint getHealthyInstanceExclude(List<ServiceEndpoint> triedEndpoints) {
        mutex.readLock().lock();
        try {
            Set<ServiceEndpoint> set = new HashSet<>(triedEndpoints);
            Set<ServiceEndpoint> seenInstances = new HashSet<>();
            while (true) {
                ServiceEndpoint retval = getHealthyInstance();
                if (true) {
                    if (seenInstances.contains(retval)) {
                        //we've made a complete loop
                        return null;
                    }
                    if (set.contains(retval)) {
                        seenInstances.add(retval);
                        continue;
                    }
                }
                return retval;
            }
        } finally {
            mutex.readLock().unlock();
        }
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void waitForServiceInstance() {
        while (true) {
            if (getHealthyInstance() != null) {
                break;
            }
            try {
                notificationSemaphore.tryAcquire(1, 100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted", e);
                break;
            }
        }
        logger.debug("Found service instance of {}", serviceName);
    }

    //intended only for debugging
    public List<AvailabilityZone> getAvailabilityZones() {
        return availabilityZones;
    }

}

class AvailabilityZone {

    private String name = "";
    private ServiceEndpointList serviceEndpoints = new ServiceEndpointList();

    public String getName() {
        return name;
    }

    public void addServiceEndpoint(ServiceEndpoint endpoint) {
        if (serviceEndpoints.isEmpty()) {
            this.name = endpoint.getAvailZone();
        }
        serviceEndpoints.add(endpoint);
    }

    //modifies state
    public ServiceEndpoint nextEndpoint() {
        return serviceEndpoints.nextAvailable();
    }

    public void updateEndpointHealth(ServiceEndpoint ep, CircuitBreakerState.State state) {
        serviceEndpoints.updateEndpointHealth(ep, state);
    }

    //intended only for debugging
    public ServiceEndpointList getServiceEndpoints() {
        return serviceEndpoints;
    }
}
