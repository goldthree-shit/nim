package com.justafewmistakes.nim.route.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
@Getter
public class AppConfiguration {

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String nacosAddress;

    @Value("${route.handler}")
    private String routeHandler;

}
