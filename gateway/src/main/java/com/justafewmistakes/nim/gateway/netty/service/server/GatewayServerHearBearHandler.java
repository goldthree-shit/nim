package com.justafewmistakes.nim.gateway.netty.service.server;

import com.justafewmistakes.nim.common.kit.HeartBeatHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * Duty: 网关服务端，过久没收到来自客户端的心跳包，断开连接
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class GatewayServerHearBearHandler implements HeartBeatHandler {
    @Override
    public void process(ChannelHandlerContext ctx) throws Exception {

    }
}
