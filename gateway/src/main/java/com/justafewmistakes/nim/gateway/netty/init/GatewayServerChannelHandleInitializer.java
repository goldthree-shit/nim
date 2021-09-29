package com.justafewmistakes.nim.gateway.netty.init;

import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import com.justafewmistakes.nim.gateway.netty.handler.GatewayClientHandler;
import com.justafewmistakes.nim.gateway.netty.handler.GatewayServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class GatewayServerChannelHandleInitializer extends ChannelInitializer<SocketChannel> {

    private final GatewayServerHandler handler = new GatewayServerHandler(); //服务端的处理器

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                // 服务端每12s没收到客户端发送来的心跳，进行一次记录。3次以后断开连接
                .addLast(new IdleStateHandler(12, 0, 0))
                //拆包解码
                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(ResponseProtocol.Response.getDefaultInstance()))
                //拆包编码
                .addLast(new ProtobufVarint32LengthFieldPrepender())
                .addLast(new ProtobufEncoder())
                .addLast(handler);
    }
}
