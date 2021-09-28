package com.justafewmistakes.nim.route.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.justafewmistakes.nim.common.routeprotocol.RouteHandler;
import com.justafewmistakes.nim.common.util.PrefixUtil;
import com.justafewmistakes.nim.common.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Configuration
public class BeanConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanConfig.class);

    @Autowired
    private AppConfiguration appConfiguration;

    @Bean
    public LoadingCache<String, String> loadingCache() {
        return CacheBuilder.newBuilder().build(new CacheLoader<String, String>() {
            @Override
            public String load(String key) throws Exception {
                return null;
            }
        });
    }

    /**
     * 获取选则网关的路由处理器
     */
    @Bean
    public RouteHandler routeHandler() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String routeHandler = appConfiguration.getRouteHandler();
        RouteHandler handler = (RouteHandler) Class.forName(routeHandler).newInstance(); //用这个初始化的不能有构造函数
        LOGGER.info("Route module now use [{}] to choose gateway", handler.getClass().getSimpleName());
        return handler;
    }

    @Bean
    public TokenUtil tokenUtil() {
        return new TokenUtil();
    }

}
