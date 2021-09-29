package com.justafewmistakes.nim.gateway.cache;

import com.google.common.cache.LoadingCache;
import com.justafewmistakes.nim.gateway.kit.NacosClient;
import com.justafewmistakes.nim.gateway.netty.client.GatewayClient;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Duty: 用于缓存现在已经连接上的服务端与对应的管道
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class IMServerCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(IMServerCache.class);

    // TODO:在管道断开的时候更新还没写
    // TODO：使用管道前要检测是否可达
    @Resource(name = "stringNioCache")
    private LoadingCache<String, NioSocketChannel> IMCacheSN; // 缓存所有的nacos上的IMServer，key是 (ip):(im_port),value是nio管道，
                                                            // 会在nacos监听的东西变化的时候/管道断开时更新

    @Resource(name = "nioStringCache")
    private LoadingCache<NioSocketChannel, String> IMCacheNS; //反向存储一份，用于管道断开时的查找



    @Autowired
    private NacosClient nacosClient; // 当缓存里面没有东西的时候就去更新

    @Autowired
    private GatewayClient gatewayClient; // gateway当作客户端，用于nacos变化的时候,进行channel的变更

    /**
     * 向缓存中新增对应的管道
     */
    public void addCache(String IMServer, NioSocketChannel channel) {
        IMCacheSN.put(IMServer, channel);
        IMCacheNS.put(channel, IMServer);
    }

    /**
     * 更新目前连接的IM服务器的缓存，如果已经存在的不变，不存在的断开，新增的去重连
     */
    public void updateCache(List<String> IMServerList) {
        for(String old : IMCacheSN.asMap().keySet()) { //旧的不存在于新的nacos中存在的服务列表中的时候，断开管道
            if(!IMServerList.contains(old)) {
                try {
                    NioSocketChannel channel = IMCacheSN.get(old);
                    IMCacheSN.invalidate(old); //当不存在于新的nacos中的时候，删去该key对应的value
                    IMCacheNS.invalidate(channel);
                    channel.close(); //TODO:测试一下这个关闭会不会触发管道中的关闭操作(会，所以要在搞一个变量让他不要重连，和用户主动退出一样)
                } catch (ExecutionException e) {
                    LOGGER.error("从IMServer中获取缓存出错");
                    e.printStackTrace();
                }
            }
        }

        try {
            gatewayClient.connectToAll(IMServerList);
        } catch (ExecutionException | InterruptedException e) {
            LOGGER.error("gateway 在nacos发生变化时连接 IM服务器产生错误，但是已经去掉了无效的，所以暂时没有问题");
        }

    }

    /**
     * 检验是否已经连接上了该IM服务器
     */
    public boolean alreadyContain(String imServer) {
        return IMCacheSN.asMap().containsKey(imServer);
    }

    /**
     * 获取全部的IM服务器管道缓存信息
     */
    public List<NioSocketChannel> getAllChannelFromCacheAsList() {
        return new ArrayList<>(IMCacheSN.asMap().values());
    }

    /**
     * 获取全部的IM服务器缓存信息,修改会产生影响
     */
    public Map<String, NioSocketChannel> getAllChannelFromCacheAsMap() {
        return IMCacheSN.asMap();
    }

    /**
     * 管道不可用的时候，通过管道删除记录
     */
    public void removeByChannel(NioSocketChannel channel) {
        try {
            String imSever = IMCacheNS.get(channel);
            IMCacheNS.invalidate(channel);
            IMCacheSN.invalidate(imSever);
        } catch (ExecutionException e) {
            LOGGER.error("获取缓存失败");
        }
    }

    /**
     * 管道可用返回其连接的服务器名
     */
    public String getServerName(NioSocketChannel channel) throws ExecutionException {
        return IMCacheNS.get(channel);
    }
}