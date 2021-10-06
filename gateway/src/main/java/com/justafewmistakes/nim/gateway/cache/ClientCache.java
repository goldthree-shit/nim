package com.justafewmistakes.nim.gateway.cache;

import com.google.common.cache.LoadingCache;
import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IMException;
import com.justafewmistakes.nim.common.util.TokenUtil;
import com.justafewmistakes.nim.gateway.kit.RedisServerKit;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Duty: 缓存连接到该网关的客户端，并且也用redis获取全部的客户端连接情况
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class ClientCache {
    //TODO:客户端缓存
    @Resource(name = "longNioClientCache")
    private LoadingCache<Long, NioSocketChannel> clientCacheLN; //客户端连接网关的缓存，是客户端id：客户端管道

    @Resource(name = "nioLongClientCache")
    private LoadingCache<NioSocketChannel, Long> clientCacheNL; //客户端连接网关的缓存，是客户端管道：客户端id

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCache.class);

    @Autowired
    private RedisServerKit redisServerKit; //全部的redis服务

    @Autowired
    private TokenUtil tokenUtil; //用于解析获取到的token中的gateway消息

    /**
     * 向缓存中新增对应的管道
     */
    public void addCache(Long clientId, NioSocketChannel channel) {
        // TODO:锁
//        synchronized (ClientCache.class) {
            clientCacheLN.put(clientId, channel);
            clientCacheNL.put(channel, clientId);
//        }
    }

    /**
     * 移除缓存中的管道
     */
    public void removeCache(Long clientId) {
        //TODO:锁
//        synchronized (ClientCache.class) {
            NioSocketChannel channel = clientCacheLN.asMap().get(clientId);
            clientCacheLN.invalidate(clientId);
            clientCacheNL.invalidate(channel);
            if(channel != null) channel.close();
//        }
    }

    /**
     * 移除缓存中的管道
     */
    public Long removeCache(NioSocketChannel channel) {
        //TODO:锁
//        synchronized (ClientCache.class) {
            Long clientId = clientCacheNL.asMap().get(channel);
            clientCacheNL.invalidate(channel);
            clientCacheLN.invalidate(clientId);
            if(channel != null) channel.close();
            return clientId;
//        }
    }

    /**
     * 服务端让用户下线（管道长时间没有心跳，或者管道断开）
     */
    public Long clientOffline(NioSocketChannel channel) {
        // 无论如何都要让缓存丢失
        Long clientId = removeCache(channel);
        // 检测是否已经下线
        boolean isOnline = redisServerKit.isOnline(clientId);
        if(isOnline) { //去让客户端下线,直接清除redis中的内容就好，管道断开会自动通知到客户端让他去重连
            redisServerKit.clientOffline(clientId);
        }
        return clientId;
    }

    /**
     * 是否客户端离线（检验缓存中是否有管道）
     * @param clientId 客户端id，无前缀
     */
    public boolean isClientOffline(Long clientId) {
        return false;
    }

    /**
     * 通过客户端id获取他连接到的网关，这个是从redis中获取的
     * TODO: 看看这个到底有没有必要
     * @param clientId 客户端id，无前缀
     */
    @Deprecated
    public String getOnlineClientGatewayInfo(Long clientId) {
        String token = redisServerKit.getOnlineTokenInfo(clientId);
        try {
            Map<String, String> map = tokenUtil.getInfoMapFromToken(token);
            return map.get("gateway");
        } catch (Exception e) {
            LOGGER.error("解析token出错",e);
        }
        return "";
    }

    /**
     * 通过客户端的id获取连接到该网关上的客户端管道
     * @param ClientId 客户端id，无前缀
     */
    public NioSocketChannel getClientChannel(Long ClientId) {
        NioSocketChannel channel = clientCacheLN.asMap().get(ClientId);
        if(channel == null) throw new IMException(FailEnums.CLIENT_NOT_FOUND);
        return channel;
    }

    /**
     * 通过管道获取连接过来的客户端id
     */
    public Long getClientId(NioSocketChannel channel) {
        return clientCacheNL.asMap().get(channel);
    }
}
