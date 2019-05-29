package com.kennyzhu.micro.framework.rpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     ServiceEndpointList   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 15:08  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */

// support multithread ops.
public class ServiceEndpointList {
    public  boolean bRobinSelect = true;
    private static final Logger logger = LoggerFactory.getLogger(ServiceEndpointList.class);

    protected volatile int size = 0;
    protected ServiceEndpointNode returnNext = null;
    protected ReentrantReadWriteLock mutex = new ReentrantReadWriteLock();

    public void add(ServiceEndpoint sep) {
        mutex.writeLock().lock();
        try {
            if (size == 0) {
                ServiceEndpointNode node = new ServiceEndpointNode(sep);
                this.returnNext = node;
                node.prev = node;
                node.next = node;
            } else {
                ServiceEndpointNode node = new ServiceEndpointNode(sep);
                ServiceEndpointNode previous = returnNext;
                returnNext = node;
                node.next = previous.next;
                node.next.prev = node;
                node.prev = previous;
                previous.next = node;
            }
            size++;
        } finally {
            mutex.writeLock().unlock();
        }
    }

    public ServiceEndpoint nextAvailable() {
        mutex.writeLock().lock(); //needs write b/c it calls canServeRequests
        try {
            ServiceEndpointNode retval = returnNext;

            // to loop service if only one?..
            for (int i = 0; i < size; i++) {
                if (retval.value.canServeRequests()) {
                    retval.value.incrementServingRequests();
                    returnNext = retval.next;
                    logger.info("nextAvailable: available node is:" + retval.value);
                    return retval.value;
                } else {
                    returnNext = returnNext.next;
                    retval = returnNext;
                }
            }

            // if only one, just return ,
            if (bRobinSelect) {
                logger.info("nextAvailable: round bin select node is:" + retval.value);
                return retval.value;
            }
            //if we got here, there are none available
            logger.info("nextAvailable: none available , the next node is:" + returnNext);
            return null;
        } finally {
            mutex.writeLock().unlock();
        }
    }

    public boolean isEmpty() {
        mutex.readLock().lock();
        try {
            return size == 0;
        } finally {
            mutex.readLock().unlock();
        }
    }

    public int size() {
        mutex.readLock().lock();
        try {
            return size;
        } finally {
            mutex.readLock().unlock();
        }
    }

    public void updateEndpointHealth(ServiceEndpoint ep, CircuitBreakerState.State state) {
        mutex.readLock().lock();
        try {
            ServiceEndpointNode current = returnNext;
            int count = size;
            for (int i = 0; i < count; i++) {
                if (current.value.getHostAndPort().equals(ep.getHostAndPort())) {
                    current.value.setCircuitBreakerState(state);
                    return;
                }
                current = current.next;
            }
            logger.error("updateEndpointHealth: endpoint not found: {}", ep.toString());
        } finally {
            mutex.readLock().unlock();
        }
    }

    //intended only for debugging
    public void debugDump(StringBuilder sb) {
        mutex.writeLock().lock();
        try {
            ServiceEndpointNode current = returnNext;
            for (int i = 0; i < size; i++) {
                sb.append("    ").append(current.toString()).append(": ").
                        append(current.value.getCircuitBreakerState()).append("\n");
                current = current.next;
            }
        } finally {
            mutex.writeLock().unlock();
        }
    }

}

// link node
class ServiceEndpointNode {
    ServiceEndpoint value;
    ServiceEndpointNode prev;
    ServiceEndpointNode next;

    public ServiceEndpointNode(ServiceEndpoint value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
