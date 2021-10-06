package com.justafewmistakes.nim.gateway.netty.init;

import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import com.justafewmistakes.nim.gateway.netty.handler.GatewayClientHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Duty: 网关的管道处理器的初始化器
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class GatewayClientChannelHandleInitializer extends ChannelInitializer<Channel> {

//    private final GatewayClientHandler handler = new GatewayClientHandler(); //不能使用依赖注入，因为netty启动的时候，没有交给ioc

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                // 客户端10s没向服务器发送消息，发送一个心跳包
                .addLast(new IdleStateHandler(0, 10, 0))
                //拆包解码
                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(ResponseProtocol.Response.getDefaultInstance())) //TODO：！！！！记得换
//                .addLast(new ProtobufDecoder(CIMResponseProto.CIMResProtocol.getDefaultInstance()))
                //拆包编码
                .addLast(new ProtobufVarint32LengthFieldPrepender())
                .addLast(new ProtobufEncoder())
                .addLast(new GatewayClientHandler());
    }
}
