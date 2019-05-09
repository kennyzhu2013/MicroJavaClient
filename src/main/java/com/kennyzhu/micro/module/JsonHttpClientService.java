package com.kennyzhu.micro.module;

import com.google.inject.Inject;
import com.kennyzhu.micro.framework.OrangeContext;
import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import com.kennyzhu.micro.framework.protobuf.RpcEnvelope;
import com.kennyzhu.micro.framework.rpc.RpcClient;
import com.kennyzhu.micro.framework.rpc.RpcClientFactory;
import com.kennyzhu.micro.framework.rpc.exception.RpcCallException;

/**
 *  
 *  * All rights Reserved, Designed By kennyzhu 13802885090@139.com
 *  * @projectName micro.helo
 *  * @title     JsonHttpClientService   
 *  * @package    com.kennyzhu.micro.module  
 *  * @description    ${TODO}  
 *  * @author kennyzhu     
 *  * @date   2019/5/9 11:54  
 *  * @version V1.0.1
 *  * @copyright 2019 www.chinamobile.com
 *  * 注意 本内容仅限于 中移互联网有限公司，禁止外泄以及用于其他的商业 
 *  
 */
public class JsonHttpClientService {
    protected RpcClientFactory  clientFactory;
    protected ServiceProperties  properties;

    @Inject
    public JsonHttpClientService(RpcClientFactory factory, ServiceProperties props) {
        this.clientFactory = factory;
        this.properties = props;
    }


    // send to server and get response.
    // @methodSuffix: for example: "Preferences/GetPreferencesList?limit=2&index=1"
    public String SendToServer(String jsonSend, String methodSuffix) {
        // method sync call
        // RpcClient<Hello.Response> rpcClient = clientFactory.newClient("go.micro.api.media-gateway", "Say.Hello", Hello.Response.class).build();
        // if not use protobuf, set hello default.
        RpcClient<RpcEnvelope.Response> rpcClient = clientFactory.newClient(properties.getServiceName(), methodSuffix, RpcEnvelope.Response.class).build();

        try {
            // 服务端Netty在测试时遇到大量java.nio.channels.ClosedChannelException异常。有可能是你的代码有问题，也有可能仅是客户端主动关闭了连接，导致服务端的写失败..
            RpcEnvelope.Request request = RpcEnvelope.Request.newBuilder().setServiceMethod(methodSuffix).setSequenceNumber(0).build();
            String response = rpcClient.callSynchronous(jsonSend, new OrangeContext());
            System.out.println(response);
            System.out.println("Call successed!");
            return response;
        }catch (RpcCallException ex) {
            System.out.println("\nGetting configuration failed, will retry. {}");
            System.out.println(ex.toString());
            return  ex.toString();
        }
        catch (Exception ex2) {
            System.out.println("\nOther exception. {}");
            System.out.println(ex2.toString());
            ex2.toString();
        }

        // impossible run here.
        return "";
    }
}
