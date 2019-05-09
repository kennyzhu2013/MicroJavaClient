package com.kennyzhu.micro.test;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.kennyzhu.micro.framework.configuration.ServiceProperties;
import com.kennyzhu.micro.framework.rpc.*;
import com.kennyzhu.micro.module.BaseApiClientInectionModule;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.kennyzhu.micro.module.JsonHttpClientService;

// App can be singleton...
public class HelloWorld {
    private final ServiceProperties props;
    private RpcClientFactory clientFactory;
    private LoadBalancerFactory loadBalancerFactory;
    private LoadBalancerImpl loadBalancer;

    // thread pool to process the request and receive response.
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

    public HelloWorld(ServiceProperties props) {
        this.props = props;
        GreeterInjectionModule helloModule = new GreeterInjectionModule(props.getServiceName(), props);

        //Use Injector to manager dependencies with your module.
        Injector injector = Guice.createInjector(helloModule); // , new ServiceRegistryModule(props));

        //LoadBalancer must be initialized, default use LoadBalancerImpl to initialize.
        loadBalancerFactory = injector.getInstance(LoadBalancerFactory.class);
        // loadBalancerFactory.initialize(provider);
        loadBalancer = (LoadBalancerImpl) loadBalancerFactory.getLoadBalancer(props.getServiceName());

        // default if no LoadBalancerUpdate, loadBalancer get service end point from registry-server, so you can not add it yourself.
        // 注意：默认情况 通过注册服务器获取的是内存里存的内网地址，此时需要手动添加service endpoints..
        LoadBalancerUpdate balancerUpdate = new LoadBalancerUpdate();

        // write f5 address here.
        balancerUpdate.addNewService(new ServiceEndpoint(executor, "120.197.230.65:8408", "microdc"));
        loadBalancer.updateServiceEndpoints(balancerUpdate);

        // write your process
        // clientFactory = injector.getInstance(RpcClientFactory.class);
        // helloModule.TestSayHello( clientFactory );
        helloModule.TestJsonHttpClientService(injector);
    }

    public static void main(String[] args) {
        // init service properties..
        // default FLAG_EXPOSE_ERRORS_HTTP = exposeErrorsHttp.
        ServiceProperties propsTemp = new ServiceProperties();

        // registry server， not usable now, just for future ...
        propsTemp.addProperty(ServiceProperties.REGISTRY_SERVER_KEY, "120.197.230.65:8500");
        propsTemp.addProperty("registry", "consul"); // etcd, etcd-v3, zookeeper and so on.
        propsTemp.setServiceName("go.micro.api.media-gateway");

        //app init
        HelloWorld helloApp = new HelloWorld(propsTemp);
        System.out.println("Hello Gradle");
    }

    // some release ops.
    public void Release() {

    }
}

// for module :一组Binder, define your module configs.
// test ClientInjectionModule.
class GreeterInjectionModule extends BaseApiClientInectionModule {
    // private HttpClient httpClient;
    public GreeterInjectionModule(String serviceName, ServiceProperties props) {
        super(serviceName, props);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(ServiceProperties.class).toInstance(serviceProperties);
        bind(LoadBalancer.class).to(LoadBalancerImpl.class);
        bind(JsonHttpClientService.class);
    }

    // test ClientInjectionModule bind..
    public void TestJsonHttpClientService(Injector injector) {
        Gson gson = new Gson();
        HelloMessage message = new HelloMessage();
        String jstring = gson.toJson(message);
        JsonHttpClientService httpClientService = injector.getInstance(JsonHttpClientService.class);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            httpClientService.SendToServer(jstring, "Preferences/GetPreferencesList?limit=2&index=1");
        }
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
    }

    // process
    /*
    public void TestSayHello(RpcClientFactory clientFactory) {
        // method sync call
        RpcClient<Hello.Response> rpcClient = clientFactory.newClient("go.micro.srv.greeter", "Say.Hello", Hello.Response.class).build();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            try {
                // 服务端Netty在测试时遇到大量java.nio.channels.ClosedChannelException异常。有可能是你的代码有问题，也有可能仅是客户端主动关闭了连接，导致服务端的写失败..
                Hello.Request request = Hello.Request.newBuilder().setName("hello world").build();
                Hello.Response response = (Hello.Response)rpcClient.callSynchronous(request, new OrangeContext());
                System.out.println(response.getMsg());
                System.out.println("Call successed!");
            }catch (RpcCallException ex) {
                System.out.println("\nGetting configuration failed, will retry. {}");
                System.out.println(ex.toString());
            }
            catch (Exception ex2) {
                System.out.println("\nOther exception. {}");
                System.out.println(ex2.toString());
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
    } */
}
