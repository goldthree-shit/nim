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
        clientCacheLN.put(clientId, channel);
        clientCacheNL.put(channel, clientId);
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
     * @param clientId 客户端id，无前缀
     */
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
}
