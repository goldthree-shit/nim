package com.justafewmistakes.nim.gateway.netty.handler;

import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.channels.SocketChannel;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@ChannelHandler.Sharable
public class GatewayClientHandler extends SimpleChannelInboundHandler<ResponseProtocol.Response> {

    /**
     * 管道失效
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    /**
     * 从管道中获取数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseProtocol.Response msg) throws Exception {

    }
}
