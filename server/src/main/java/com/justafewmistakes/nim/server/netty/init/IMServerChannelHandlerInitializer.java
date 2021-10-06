package com.justafewmistakes.nim.server.netty.init;

import com.justafewmistakes.nim.common.protobuf.RequestProtocol;
import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import com.justafewmistakes.nim.server.netty.handler.IMServerChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Duty:用于初始化im服务器的管道
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
public class IMServerChannelHandlerInitializer extends ChannelInitializer<Channel> {

//    private final IMServerChannelHandler handler = new IMServerChannelHandler(); //不能使用依赖注入，因为netty启动的时候，没有交给ioc

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                // im服务器每13s没收到网关发送来的心跳，进行一次记录。3次以后断开连接
                .addLast(new IdleStateHandler(13, 0, 0))
                //拆包解码
                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(RequestProtocol.Request.getDefaultInstance())) //TODO：！！！！记得换掉
//                .addLast(new ProtobufDecoder(CIMRequestProto.CIMReqProtocol.getDefaultInstance()))
                //拆包编码
                .addLast(new ProtobufVarint32LengthFieldPrepender())
                .addLast(new ProtobufEncoder())
                .addLast(new IMServerChannelHandler());
    }
}
