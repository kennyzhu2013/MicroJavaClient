/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     LoadBalancerUpdate   
 *  * @package    com.kennyzhu.micro.framework.rpc  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/6 16:00  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */

package com.kennyzhu.micro.framework.rpc;


import java.util.ArrayList;
import java.util.List;


public class LoadBalancerUpdate {
    protected List<ServiceEndpoint> newServices = new ArrayList<>();
    protected List<ServiceEndpoint> updatedServices = new ArrayList<>();
    protected List<ServiceEndpoint> deletedServices = new ArrayList<>();

    public void addNewService(ServiceEndpoint ep) {
        newServices.add(ep);
    }

    public void addUpdatedService(ServiceEndpoint ep) {
        updatedServices.add(ep);
    }

    public void addDeletedService(ServiceEndpoint ep) {
        deletedServices.add(ep);
    }

    public List<ServiceEndpoint> getNewServices() {
        return newServices;
    }

    public List<ServiceEndpoint> getUpdatedServices() {
        return updatedServices;
    }

    public List<ServiceEndpoint> getDeletedServices() {
        return deletedServices;
    }

    public boolean isEmpty() {
        return newServices.isEmpty() && updatedServices.isEmpty() &&
                deletedServices.isEmpty();
    }
}

