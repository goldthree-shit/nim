package com.justafewmistakes.nim.server.controller;

import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.config.impl.ServerListManager;
import com.justafewmistakes.nim.common.api.CommonResult;
import com.justafewmistakes.nim.route.RouteApi;
import com.justafewmistakes.nim.server.IMServerApi;
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
public class IMServerController implements IMServerApi {
    @Override
    public CommonResult<List<String>> getAllLinkedGateway() {
        return null;
    }

    @Override
    public CommonResult<List<String>> getAllGatewayInNacos() {
        return null;
    }
}
