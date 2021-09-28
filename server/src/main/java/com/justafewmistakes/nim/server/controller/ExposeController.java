package com.justafewmistakes.nim.server.controller;

import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.config.impl.ServerListManager;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Properties;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@RestController
public class ExposeController extends ZoneAvoidanceRule {
    @Autowired
    DiscoveryClient discoveryClient;

    @GetMapping("/hello")
    public void getHello() throws NacosException {
//        Properties properties = new Properties();
//        properties.setProperty("serverAddr", "localhost:8848");
//        properties.setProperty("namespave", "demo");
//        NamingService namingFactory = NamingFactory.createNamingService(properties);
//        namingFactory.getSubscribeServices();
//        String serverIp = "127.0.0.1";
//        int serverPort = 8848;
//        String serverAddr = serverIp + ":" + serverPort;
//        String serviceName = "nacos-sdk-java-discovery";
//        NamingService namingService = NamingFactory.createNamingService(serverAddr);
//        namingService.registerInstance(serviceName, serverIp, serverPort);

//        Instance instance = new Instance();
//        instance.setIp(serverIp);//IP
//        instance.setPort(serverPort);//端口
//        instance.setServiceName(serviceName);//服务名
//        instance.setEnabled(true);//true: 上线 false: 下线
//        instance.setHealthy(true);//健康状态
//        instance.setWeight(1.0);//权重
//        instance.addMetadata("nacos-sdk-java-discovery1", "true");//元数据
//        NamingService namingService1 = NamingFactory.createNamingService(serverAddr);
//        namingService.registerInstance(serviceName, instance);

//        List<Server> allServers = this.getLoadBalancer().getAllServers();

        List<String> services = discoveryClient.getServices();
        List<ServiceInstance> instances;
        for(String now : services) {
            instances = discoveryClient.getInstances(now);
            instances.get(0).getMetadata();
        }
    }
}
