package com.justafewmistakes.nim.server.netty.handler;

import com.justafewmistakes.nim.common.protobuf.RequestProtocol;
import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import com.justafewmistakes.nim.common.util.HearBeatUtil;
import com.justafewmistakes.nim.server.cache.GatewayCache;
import com.justafewmistakes.nim.server.kit.SpringBeanFactory;
import com.justafewmistakes.nim.server.netty.service.server.IMServerHeartBeatHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Duty: im服务器的自定义管道处理器
 * TODO: 将3个handler的属性设置一下
 * @author justafewmistakes
 * Date: 2021/10
 */
//@ChannelHandler.Sharable
public class IMServerChannelHandler extends SimpleChannelInboundHandler<RequestProtocol.Request> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IMServerChannelHandler.class);

//    private final GatewayCache gatewayCache = SpringBeanFactory.getBean(GatewayCache.class); //连接的网关缓存,不能这么创建，因为netty生成的比这个早

//    private final IMServerHeartBeatHandler heartBeatHandler = SpringBeanFactory.getBean(IMServerHeartBeatHandler.class); //心跳处理器，这里就是用于读空闲过久让网关断开的

    /**
     * 管道失效
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 可能读空闲太久已经让他断开了
        GatewayCache gatewayCache = SpringBeanFactory.getBean(GatewayCache.class);
        String gateway = gatewayCache.gatewayOffline((NioSocketChannel) ctx.channel());
        LOGGER.error("temp服务端断开连接temp");
        if(gateway != null) {
            LOGGER.error("网关[{}]断开连接",gateway);
            if(ctx.channel().isActive()) ctx.channel().close();
        }
        super.channelInactive(ctx);
    }

    /**
     * 管道首次活跃
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(ctx.channel() instanceof NioSocketChannel)
            LOGGER.info("不知道哪个网关客户端连接到网关服务端成功,管道首次活跃触发");
    }

    /**
     * 读写超时处理
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            //网关服务端的handler，如果是读空闲，则3次后断开连接
            if(event.state() == IdleState.READER_IDLE) {
                LOGGER.warn("网关服务端读空闲");

                ResponseProtocol.Response response = ResponseProtocol.Response.newBuilder()
                        .setResponseMsg("cnmcnmcnmcnm")
                        .build();
                ctx.channel().writeAndFlush(response);

//                ctx.channel().close();
//                LOGGER.error("cnm服务端断进行开连接temp");
                IMServerHeartBeatHandler heartBeatHandler = SpringBeanFactory.getBean(IMServerHeartBeatHandler.class);
                heartBeatHandler.process(ctx);
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RequestProtocol.Request msg) throws Exception {
        LOGGER.error("im服务器收到【{}】",msg.toString());

        HearBeatUtil hearBeatUtil = SpringBeanFactory.getBean(HearBeatUtil.class);
        ResponseProtocol.Response pong = hearBeatUtil.Pong();
        ctx.writeAndFlush(pong);
    }
}
