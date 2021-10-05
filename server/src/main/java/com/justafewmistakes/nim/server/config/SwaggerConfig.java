package com.justafewmistakes.nim.server.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.justafewmistakes.nim.common.config.BaseSwaggerConfig;
import com.justafewmistakes.nim.common.config.SwaggerProperties;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Duty: 文档
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@EnableKnife4j
@EnableSwagger2
@Configuration
public class SwaggerConfig extends BaseSwaggerConfig {

    @Override
    public SwaggerProperties setSwaggerProperties() {
        SwaggerProperties properties = new SwaggerProperties();
        properties.setPath("com.justafewmistakes.nim.server.controller");
        return properties;
    }
}
