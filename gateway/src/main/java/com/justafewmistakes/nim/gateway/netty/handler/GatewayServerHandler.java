package com.justafewmistakes.nim.gateway.netty.handler;

import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@ChannelHandler.Sharable
@Component
public class GatewayServerHandler extends SimpleChannelInboundHandler<ResponseProtocol.Response> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseProtocol.Response msg) throws Exception {

    }
}
