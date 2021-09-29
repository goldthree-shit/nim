package com.justafewmistakes.nim.gateway.kit;

import com.justafewmistakes.nim.gateway.cache.IMServerCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Duty:检测是否可达的组件
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class ReachableKit {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReachableKit.class);

    @Autowired
    private IMServerCache imServerCache;

    @Autowired
    private NacosClient nacosClient;

    /**
     * 验证该IM服务器是否可达，不可达则更新
     * @param IMServer
     * @return
     */
    private boolean isIMServerReachable(String IMServer) {
        String[] addr = IMServer.split(":");
        String ip = addr[0], port = addr[1];
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 1000);
            return true;
        } catch (IOException e) {
            LOGGER.error("ip为[{}]，端口为[{}]的IMServer不可用，从nacos更新新的缓存", ip, port);
            imServerCache.updateCache(nacosClient.getAllServersInNacos().get("server:"));
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
