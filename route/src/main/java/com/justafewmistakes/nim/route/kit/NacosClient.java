package com.justafewmistakes.nim.route.kit;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.justafewmistakes.nim.route.cache.GatewayCache;
import com.justafewmistakes.nim.route.config.AppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Duty: 获取nacos上的配置信息的组件
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class NacosClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosClient.class);

    @Autowired
    private GatewayCache gatewayCache; //用于nacos注册中心变化的时候改变缓存

    @Autowired
    DiscoveryClient discoveryClient; //nacos的客户端

    @Autowired
    private AppConfiguration appConfiguration;

    /**
     * 返回 所有的 注册在nacos上的服务
     */
    public Map<String, List<String>> getAllServersInNacos() {
        Map<String, List<String>> map = new HashMap<>();
        List<String> services = discoveryClient.getServices();//获取注册在nacos上的服务
        for(String service : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(service); //对每一种服务都有多个实例
            List<String> list = new ArrayList<>();
            for(ServiceInstance instance : instances) {
                String im_addr = instance.getMetadata().get("im_addr"); //类似 route:${nim.ip}:${nim.port}
                list.add(im_addr);
            }
            map.put(service, list);
        }
        return map;
    }

    /**
     * 注册监听，并且在发生变化的时候，通知缓存进行更改
     */
    public void subscribe() {
        try {
            // 注册监听
            NamingService namingService = NamingFactory.createNamingService(appConfiguration.getNacosAddress());
            namingService.subscribe("gateway:","DEFAULT_GROUP",new EventListener() {
                @Override
                public void onEvent(Event event) {
                    if (event instanceof NamingEvent) {
                        LOGGER.info("nacos注册中心的网关发生变化,去更新网关的缓存");
                        LOGGER.info("目前服务名："+((NamingEvent)event).getServiceName());
                        LOGGER.info("目前实例："+((NamingEvent)event).getInstances());
                        List<Instance> instances = ((NamingEvent) event).getInstances();
                        List<String> gatewayList = new ArrayList<>();
                        for(Instance instance : instances) {
                            if(instance.isHealthy()) gatewayList.add(instance.getMetadata().get("im_addr"));
                        }
                        gatewayCache.updateCache(gatewayList);
                    }
                }
            });

        } catch (NacosException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


}
