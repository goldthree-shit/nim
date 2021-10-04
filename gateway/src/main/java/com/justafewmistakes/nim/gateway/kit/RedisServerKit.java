package com.justafewmistakes.nim.gateway.kit;

import com.google.common.base.Strings;
import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.gateway.config.AppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Duty: 路由端所有redis服务
 * TODO: 看看这个到底有没有必要
 * @author justafewmistakes
 * Date: 2021/10
 */
@Component
public class RedisServerKit {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AppConfiguration appConfiguration;

    /**
     * FIXME：检查是否在线,可能会存在已经登入，但是管道还没有连接上的情况
     * @param clientId 要发送消息的接收者id/
     */
    public boolean isOnline(Long clientId) {
        String preClientId = Constants.LOGIN_PREFIX + clientId;
        return !Strings.isNullOrEmpty(redisTemplate.opsForValue().get(preClientId));
    }

    /**
     * 在网关里，让客户端下线（读空闲太久了）
     */
    public void clientOffline(Long clientId) {
        redisTemplate.delete(String.valueOf(clientId));
    }

    /**
     * 检查是否有离线消息，并且是否是在本地
     * @param destination 离线客户端的id
     * @return 当无离线的时候返回0,有离线且在本地的时候返回1,有离线不在本地返回2
     */
    @Deprecated
    public int haveOfflineMsg(Long destination) {
        String preDestination = Constants.OFFLINE_MSG_PREFIX + destination;
        String gateway = redisTemplate.opsForValue().get(preDestination);
        if(gateway == null || gateway.equals("")) return 0;
        else if(gateway.equals(appConfiguration.getGatewayIp() + ":" + appConfiguration.getGatewayPort())) return 1;
        else return 2;
    }

    /**
     * 在已经有离线消息的基础上，获取离线消息所在的网关
     * @param destination 离线客户端的id
     * @return
     */
    @Deprecated
    public String offlineMsgPosition(Long destination) {
        String preDestination = Constants.OFFLINE_MSG_PREFIX + destination;
        return redisTemplate.opsForValue().get(preDestination);
    }

    /**
     * 通过客户端id获取他的token信息（目前是为了获取连接到的网关）
     * @param clientId 无前缀的客户端id
     */
    @Deprecated
    public String getOnlineTokenInfo(Long clientId) {
        String key = Constants.LOGIN_PREFIX + clientId;
        return redisTemplate.opsForValue().get(key);
    }


}
