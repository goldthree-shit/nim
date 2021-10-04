package com.justafewmistakes.nim.gateway.netty.service.server;

import com.justafewmistakes.nim.common.kit.HeartBeatHandler;
import com.justafewmistakes.nim.gateway.cache.ClientCache;
import com.justafewmistakes.nim.gateway.config.AppConfiguration;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Component("gatewayServerHeartBeatHandler")
public class GatewayServerHeartBeatHandler implements HeartBeatHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayServerHeartBeatHandler.class);

    @Autowired
    private AppConfiguration appConfiguration;

    @Autowired
    private ClientCache clientCache; //客户端连接缓存，用于让客户端下线

    // 11秒一次读空闲
    private static final int tenSecond = 11000;

    /**
     * 网关服务端的心跳处理器，用于多次读空闲后断开连接
     * @param ctx 管道上下文
     */
    @Override
    public void process(ChannelHandlerContext ctx) throws Exception {
        // 目前已经多久了
        String str = (String) ctx.channel().attr(AttributeKey.valueOf("readerTime")).get();
        if(str != null) { //判断不为null
            // 最多可以读空闲多久
            int readIdle = appConfiguration.getReadIdle();
            long maxTime = (long) readIdle * tenSecond;

            long lastTime = Long.parseLong(str);
            long now = System.currentTimeMillis(); // FIXME:这里时间用的是系统时间，记得后面修改
            long time = now - lastTime;
            if(time > maxTime) {
                Long clientId = clientCache.clientOffline((NioSocketChannel) ctx.channel());
                if(clientId != null) LOGGER.error("客户端[{}]读空闲过久,让客户端下线（redis中删除），并关闭管道", clientId);
                else LOGGER.error("客户端读空闲过久,但是已经是下线的了");
                ctx.channel().close(); //关闭管道，会通知到客户端 TODO：1.看看会不会进inactive方法 2.客户端sdk需要对此做出反应
            }
        }

    }
}
