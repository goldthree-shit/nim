package com.justafewmistakes.nim.route.cache;

import com.google.common.cache.LoadingCache;
import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.util.PrefixUtil;
import com.justafewmistakes.nim.route.kit.NacosClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Duty: 路由缓存所有的网关
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class GatewayCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayCache.class);

    @Autowired
    private LoadingCache<String, String> cache; // 缓存所有的nacos上的网关，key和value都是 (ip):(im_port)，会在nacos监听的东西变化的时候更新

    @Autowired
    private NacosClient nacosClient; //当缓存里面没有东西的时候就去更新

    /**
     * 向cache中设置缓存
     */
    public void addCache(String key) {
        cache.put(key, key);
    }

    /**
     * 获取可用网关列表
     * fresh为true，说明强制从nacos去获取列表
     */
    public List<String> getGatewayList(boolean fresh) {
        List<String> preGatewayList = new ArrayList<>();
        List<String> gatewayList = new ArrayList<>();
        if(cache.size() == 0 || fresh) { //为0/强制 就去nacos获取
            cache.invalidateAll();
            Map<String, List<String>> map = nacosClient.getAllServersInNacos();
            if(map.containsKey(Constants.GATEWAY_PREFIX)) preGatewayList = map.get(Constants.GATEWAY_PREFIX);
            for(String preGateway : preGatewayList) {
                String gateWay = PrefixUtil.parsePreGatewayToGateWay(preGateway);
                addCache(gateWay);
                gatewayList.add(gateWay);
            }
        }
        else {
            for(Map.Entry<String, String> entry : cache.asMap().entrySet()) {
                gatewayList.add(PrefixUtil.parsePreGatewayToGateWay(entry.getKey()));
            }
        }
        return gatewayList;
    }

    /**
     * 当不可达/nacos服务器有变化的时候，去更新缓存
     */
    public void updateCache(List<String> gatewayList) {
//        synchronized (GatewayCache.class) {
            cache.invalidateAll();
            for(String gateway : gatewayList) {
                addCache(PrefixUtil.parsePreGatewayToGateWay(gateway));
            }
//        }
    }
}
