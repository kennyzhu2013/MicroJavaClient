/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     GoTimer   
 *  * @package    com.kennyzhu.micro.framework.metrics  
 *  * @description  统计远程调用成功和失败时长
 *  * @author kennyzhu     
 *  * @date   2019/5/7 16:10  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
package com.kennyzhu.micro.framework.metrics;

import com.codahale.metrics.Timer;

import java.util.concurrent.TimeUnit;

public class GoTimer {
    //The structure of codahale metrics don't mesh with our go metrics.
    //To track failures and successes in different buckets requires using
    //  multiple underlying Timer objects;
    protected Timer successTimer;
    protected Timer failureTimer;
    protected String name;

    public GoTimer(String name) {
        this.name = name;
        reset();
    }

    public long start() {
        return System.nanoTime();
    }

    public void recordSuccess(long startTime) {
        successTimer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
    }

    public void recordFailure(long startTime) {
        failureTimer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
    }

    public Timer getSuccessTimer() {
        return successTimer;
    }

    public Timer getFailureTimer() {
        return failureTimer;
    }

    public String getName() {
        return name;
    }

    public void reset() {
        successTimer = new Timer();
        failureTimer = new Timer();
    }
}
