package com.justafewmistakes.nim.server.config;

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
    private String IMServerName;

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosAddress;

    @Value("${route.handler}")
    private String routeHandler;

    @Value("${nim.ip}")
    private String imServerIp;

    @Value("${nim.port}")
    private String imServerPort;

    @Value("${thread.pool.core}")
    private int core;

    @Value("${thread.pool.maxi}")
    private int maxCore;

    @Value("${thread.pool.que}")
    private int queSize;

    @Value("${nim.retry}")
    private int retryTime;

    @Value("${nim.read.idle}")
    private int readIdle;
}
