package com.justafewmistakes.nim.server.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.justafewmistakes.nim.common.routeprotocol.RouteHandler;
import com.justafewmistakes.nim.common.util.HearBeatUtil;
import com.justafewmistakes.nim.common.util.OfflineMsgUtil;
import com.justafewmistakes.nim.common.util.TokenUtil;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

    @Bean("stringNioCache")
    public LoadingCache<String, NioSocketChannel> GatewayCacheSN() {
        return CacheBuilder.newBuilder().build(new CacheLoader<String, NioSocketChannel>() {

            @Override
            public NioSocketChannel load(String key) throws Exception {
                return null;
            }
        });
    }

    @Bean("nioStringCache")
    public LoadingCache<NioSocketChannel, String> GatewayCacheNS() {
        return CacheBuilder.newBuilder().build(new CacheLoader<NioSocketChannel, String> () {

            @Override
            public String load(NioSocketChannel key) throws Exception {
                return null;
            }
        });
    }

    /**
     * 获取选则IM服务器的路由处理器
     */
    @Bean
    public RouteHandler routeHandler() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String routeHandler = appConfiguration.getRouteHandler();
        RouteHandler handler = (RouteHandler) Class.forName(routeHandler).newInstance(); //用这个初始化的不能有构造函数
        LOGGER.info("Route module now use [{}] to choose IMServer", handler.getClass().getSimpleName());
        return handler;
    }

    /**
     * 通用redis操作器
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public TokenUtil tokenUtil() {
        return new TokenUtil();
    }

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(appConfiguration.getQueSize());
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("线程池-第 %d 个线程").build();
        return new ThreadPoolExecutor(appConfiguration.getCore(), appConfiguration.getMaxCore(),
                1, TimeUnit.MINUTES, blockingQueue, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 心跳生成工具
     */
    @Bean
    public HearBeatUtil hearBeatUtil() {
        return new HearBeatUtil();
    }

    /**
     * 离线消息生成工具
     */
    @Bean
    public OfflineMsgUtil offlineMsgUtil() {
        return new OfflineMsgUtil();
    }

}
