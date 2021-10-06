package com.justafewmistakes.nim.server.cache;

import com.google.common.cache.LoadingCache;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Duty: 缓存连接上该im服务器的全部网关
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Component
public class GatewayCache {

    private final static Logger LOGGER = LoggerFactory.getLogger(GatewayCache.class);

    @Autowired
    private LoadingCache<String, NioSocketChannel> gatewayCacheSN; //存储着网关的ip：port 对应的 channel

    @Autowired
    private LoadingCache<NioSocketChannel, String> gatewayCacheNS; //反向存储一份

    /**
     * 加入缓存
     * @param gateway 网关的ip：port
     * @param channel 对应的管道
     */
    public void addCache(String gateway, NioSocketChannel channel) {
        // TODO:锁
//        synchronized (GatewayCache.class) {
            gatewayCacheSN.put(gateway, channel);
            gatewayCacheNS.put(channel, gateway);
//        }
    }

    /**
     * 通过管道移除缓存
     * @param channel 对应的管道
     * @return 有移除东西的时候返回true
     */
    public boolean removeCacheByChannel(NioSocketChannel channel) {
        // TODO:锁
//        synchronized (GatewayCache.class) {
            String gateway = gatewayCacheNS.asMap().get(channel);
            if(gateway == null) return false;
            gatewayCacheNS.invalidate(channel);
            gatewayCacheSN.invalidate(gateway);
            if(channel != null) channel.close();
            return true;
//        }
    }

    /**
     * 客户端下线了,移除缓存，并返回网关标识（ip：port）
     * @param channel 断开的网关管道
     * @return 网关的ip：port
     */
    public String gatewayOffline(NioSocketChannel channel) {
        String gateway = gatewayCacheNS.asMap().get(channel);
        removeCacheByChannel(channel);
        return gateway;
    }

    public List<NioSocketChannel> tempGet() {
        return new ArrayList<>(gatewayCacheSN.asMap().values());
    }

}
