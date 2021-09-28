package com.justafewmistakes.nim.gateway.netty.service.client;

import com.justafewmistakes.nim.common.kit.HeartBeatHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Duty: 网关客户端，过久没收到来自服务端的心跳包，重连
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class GatewayClientHeartBeatHandler implements HeartBeatHandler {
    @Override
    public void process(ChannelHandlerContext ctx) throws Exception {

    }
}
