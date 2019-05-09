package com.kennyzhu.micro.framework.rpc.backoff;

import java.time.Duration;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     RetryBackOffFunction   
 *  * @package    com.kennyzhu.micro.framework.rpc.backoff  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/7 15:20  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public interface RetryBackOffFunction {
    /**
     * Calculate the wait time between retries.  After the first attempt has been make, the
     * value of retryCounter here will be 0, then monotonically increasing.
     */
    Duration timeout(int retryCounter);

    default void execute(int retryCounter) {
        Duration timeout = timeout(retryCounter);
        if (timeout == null || timeout.isNegative()) {
            throw new IllegalArgumentException("Retry timeout cannot be null or negative.");
        }

        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout.toMillis();

        // we are in a while loop here to protect against spurious interrupts
        while (!Thread.currentThread().isInterrupted()) {
            long now = System.currentTimeMillis();
            if (now >= endTime) {
                break;
            }
            try {
                Thread.sleep(endTime - now);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // we should probably quit if we are interrupted?
                return;
            }
        }
    }
}
