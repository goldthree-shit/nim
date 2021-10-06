package com.justafewmistakes.nim.server.netty.service.server;

import com.justafewmistakes.nim.common.kit.HeartBeatHandler;
import com.justafewmistakes.nim.server.cache.GatewayCache;
import com.justafewmistakes.nim.server.config.AppConfiguration;
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
@Component
public class IMServerHeartBeatHandler implements HeartBeatHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IMServerHeartBeatHandler.class);

    @Autowired
    private AppConfiguration appConfiguration;

    @Autowired
    private GatewayCache gatewayCache; //网关连接缓存，用于让客户端下线

    // 11秒一次读空闲
    private static final int tenSecond = 13000;

    /**
     * 网关服务端的心跳处理器，用于多次读空闲后断开连接
     * @param ctx 管道上下文
     */
    @Override
    public void process(ChannelHandlerContext ctx) throws Exception {
        // 目前已经多久了
        AttributeKey<String> ATTR_KEY_READER_TIME = AttributeKey.valueOf("readerTime");
        String str = ctx.channel().attr(ATTR_KEY_READER_TIME).get();
        if(str != null) { //判断不为null
            // 最多可以读空闲多久
            int readIdle = appConfiguration.getReadIdle();
            long maxTime = (long) readIdle * tenSecond;

            long lastTime = Long.parseLong(str);
            long now = System.currentTimeMillis(); // FIXME:这里时间用的是系统时间，记得后面修改
            long time = now - lastTime;
            if(time > maxTime) {
                String gatewayOffline = gatewayCache.gatewayOffline((NioSocketChannel) ctx.channel());
                if(gatewayOffline != null) LOGGER.error("网关[{}]读空闲过久,让网关下线（redis中删除），并关闭管道", gatewayOffline);
                else LOGGER.error("客户端读空闲过久,但是已经是下线的了");
                ctx.channel().close(); //关闭管道，会通知到网关端 TODO：1.看看会不会进inactive方法 2.网关需要对此做出反应
            }
        }

    }
}
