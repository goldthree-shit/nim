package com.justafewmistakes.nim.gateway.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Getter
@Configuration
public class AppConfiguration {

    @Value("${nim.name}")
    private String gatewayName;

    @Value("${nim.que.size}")
    private int gatewayQueSize;

    @Value("${nim.filePath}")
    private String filePath;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosAddress;

    @Value("${route.handler}")
    private String routeHandler;

    @Value("${nim.ip}")
    private String gatewayIp;

    @Value("${nim.port}")
    private String gatewayPort;

    @Value("${thread.pool.core}")
    private int core;

    @Value("${thread.pool.maxi}")
    private int maxCore;

    @Value("${thread.pool.que}")
    private int queSize;

    @Value("${nim.retry}")
    private int retryTime;
}
