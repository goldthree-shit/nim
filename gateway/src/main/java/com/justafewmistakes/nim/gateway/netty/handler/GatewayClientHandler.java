package com.justafewmistakes.nim.gateway.netty.handler;

import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.protobuf.RequestProtocol;
import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import com.justafewmistakes.nim.common.util.HearBeatUtil;
import com.justafewmistakes.nim.gateway.cache.ClientCache;
import com.justafewmistakes.nim.gateway.cache.IMServerCache;
import com.justafewmistakes.nim.gateway.config.AppConfiguration;
import com.justafewmistakes.nim.gateway.kit.GatewayMsgListener;
import com.justafewmistakes.nim.gateway.kit.ReachableKit;
import io.netty.channel.ChannelFutureListener;
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

import java.nio.channels.SocketChannel;

/**
 * Duty:路由作为客户端的时候的处理器
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@ChannelHandler.Sharable
@Component
public class GatewayClientHandler extends SimpleChannelInboundHandler<ResponseProtocol.Response> {

    private final static Logger LOGGER = LoggerFactory.getLogger(GatewayClientHandler.class);

    @Autowired
    private GatewayMsgListener gatewayMsgListener; //网关消息监听，用于异步记录离线消息

    @Autowired
    private HearBeatUtil hearBeatUtil; //心跳包创建工具

    @Autowired
    private IMServerCache imServerCache; //用于管道不可用时，删除管道

    @Autowired
    private ClientCache clientCache; //客户端缓存，检验是否在线

    @Autowired
    private AppConfiguration appConfiguration;

    /**
     * 读写超时处理
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            //客户端的handler，如果是写空闲，则发送心跳包
            if(event.state() == IdleState.WRITER_IDLE) {
                RequestProtocol.Request ping = hearBeatUtil.Ping();

                ctx.writeAndFlush(ping).addListener((ChannelFutureListener) future -> {
                    if(!future.isSuccess()) {
                        LOGGER.error("客户端发送心跳失败，关闭channel");
                        future.channel().close();
                    }
                });
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    /**
     * 管道首次活跃
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if(ctx.channel() instanceof NioSocketChannel) LOGGER.info("网关客户端连接到服务端"+imServerCache.getServerName((NioSocketChannel) ctx.channel())+"成功,管道首次活跃触发");
        super.channelActive(ctx);
    }

    /**
     * 管道失效
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        if(ctx.channel() instanceof NioSocketChannel) imServerCache.removeByChannel((NioSocketChannel) ctx.channel()); //从缓存中移除管道


        super.channelInactive(ctx);
    }

    /**
     * 从管道中获取数据
     * TODO：先写在这里，客户端自己去缓存所有的数据，但是如果离线了，就由服务端缓存至多1天的数据(去redis上缓存一份元数据，连接上的不一定是同个服务器)，当客户端上线的时候，检查服务端是否有该用户的数据，有则推送后删除
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseProtocol.Response msg) throws Exception {
        // FIXME:临时的，也不是在这，是当服务端的时候，才会去搞(是在这，服务端获取到管道，在这从缓存中获取到对应的管道发回去）
        int type = msg.getType();
        // 不是pong包的时候处理，是pong包不管
        if(type != Constants.PONG) {
            Long senderId = msg.getResponseId(); //即为发送端的id
            Long destination = msg.getDestination(); //即为客户端的id
            String senderName = msg.getResponseName();
            String message = msg.getResponseMsg();

            ResponseProtocol.Response.newBuilder()
                    .setResponseId(appConfiguration.getGatewayId())
                    .setResponseName(senderName)
                    .setResponseMsg(message)
                    .setType(type)
                    .setDestination(destination)
                    .build();

            //TODO: 发送数据的操作未完成
        }
        /*
        String destination = "clientIdTest"; //客户端id，这里显然是为了测试
        String message = msg.getResponseMsg();
        gatewayMsgListener.listen(Constants.OFFLINE_MSG_PREFIX+destination, message);
         */
    }
}
