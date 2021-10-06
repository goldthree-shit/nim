package com.justafewmistakes.nim.gateway.netty.handler;

import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.protobuf.RequestProtocol;
import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import com.justafewmistakes.nim.common.routeprotocol.RouteHandler;
import com.justafewmistakes.nim.common.util.HearBeatUtil;
import com.justafewmistakes.nim.common.util.NtpUtil;
import com.justafewmistakes.nim.common.util.OfflineMsgUtil;
import com.justafewmistakes.nim.gateway.cache.ClientCache;
import com.justafewmistakes.nim.gateway.cache.IMServerCache;
import com.justafewmistakes.nim.gateway.config.AppConfiguration;
import com.justafewmistakes.nim.gateway.kit.GatewayMsgListener;
import com.justafewmistakes.nim.gateway.kit.GatewayMsgRecorder;
import com.justafewmistakes.nim.gateway.kit.RedisServerKit;
import com.justafewmistakes.nim.gateway.kit.SpringBeanFactory;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * Duty:路由作为客户端的时候的处理器
 * TODO: 将3个handler的属性设置一下
 * @author justafewmistakes
 * Date: 2021/09
 */
@ChannelHandler.Sharable
//public class gatewaywayClientHandler extends SimpleChannelInboundHandler<CIMResponseProto.CIMResProtocol> {
public class GatewayClientHandler extends SimpleChannelInboundHandler<ResponseProtocol.Response> { //TODO:!!!!记得换掉

    private final static Logger LOGGER = LoggerFactory.getLogger(GatewayClientHandler.class);
    //TODO:尝试把缓存改为concurrentHashMap
//    private final HearBeatUtil hearBeatUtil = SpringBeanFactory.getBean(HearBeatUtil.class); //心跳包创建工具,不能这么创建，因为netty生成的比这个早

//    private final IMServerCache imServerCache = SpringBeanFactory.getBean(IMServerCache.class); //用于管道不可用时，删除管道与获取连接向im服务器的管道

//    private final ClientCache clientCache = SpringBeanFactory.getBean(ClientCache.class); //用于获取所有与客户端管道相关的东西

    /**
     * 读写超时处理
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // TODO:这个是为了检测到底为什么不活跃了（读空闲但是写不空闲）
        IMServerCache imServerCache = SpringBeanFactory.getBean(IMServerCache.class);
        List<NioSocketChannel> list = imServerCache.tempGet();
        for(NioSocketChannel channel : list) {
            LOGGER.error("一个管道" + channel.toString() + channel.isActive());
        }

        if(evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;

            //客户端的handler，如果是写空闲，则发送心跳包
            if(event.state() == IdleState.WRITER_IDLE) {

                LOGGER.warn("客户端写空闲,发送ping包保活");
//                HearBeatUtil hearBeatUtil = SpringBeanFactory.getBean(HearBeatUtil.class);
//                RequestProtocol.Request ping = hearBeatUtil.Ping();
                ctx.channel().writeAndFlush(RequestProtocol.Request.newBuilder()
                        .setRequestId(-1)
                        .setGroupId(-1)
                        .setRequestName("")
                        .setRequestMsg("PING")
                        .setType(Constants.PING)
                        .setDestination(-1)
                        .setTransit("")
//                        .setSendTime(NtpUtil.getNtpTime())
                        .setRequestMsgId(0)
                        .build()).addListener((ChannelFutureListener) future -> {
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
        if(ctx.channel() instanceof NioSocketChannel) {
            LOGGER.info("网关客户端连接到不知道哪个im服务器成功,管道首次活跃触发");
        }
    }

    /**
     * 管道失效,每次断开都会去无限重连im服务器，直到成功连接所有的im服务器，在这期间是无法提供服务的
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        LOGGER.error("temp网关断开连接temp");
//        if(ctx.channel() instanceof NioSocketChannel) {
//            // 如果是在这里掉线（说明是服务器好的，但是管道断开了），而不是从nacos上关闭服务器，就需要执行重连
//            IMServerCache imServerCache = SpringBeanFactory.getBean(IMServerCache.class);
//            boolean isRemove = imServerCache.removeByChannel((NioSocketChannel) ctx.channel()); //从缓存中移除管道
//            if(isRemove) imServerCache.reconnect();
//        }


        super.channelInactive(ctx);
    }

    /**
     * 从管道中获取数据
     * TODO：先写在这里，客户端自己去缓存所有的数据，但是如果离线了，就由服务端缓存至多1天的数据(去redis上缓存一份元数据，连接上的不一定是同个服务器)，当客户端上线的时候，检查服务端是否有该用户的数据，有则推送后删除
     */
    @Override
    @SuppressWarnings("all")
    protected void channelRead0(ChannelHandlerContext ctx, ResponseProtocol.Response msg) throws Exception { //TODO:!!!!记得换掉
//    protected void channelRead0(ChannelHandlerContext ctx, CIMResponseProto.CIMResProtocol msg) throws Exception {
        LOGGER.error("网关收到【{}】",msg.toString());

        int type = msg.getType();

        // 是pong包不管，他也不可能收到ping包与确认连接包
        if(type == Constants.PONG) {return;}

        long senderId = msg.getResponseId(); //即为发送端的id
        long destination = msg.getDestination(); //即为接收端的id，发送到im服务器上的时候会变为目的网关地址+接受端id（gateway：userid）
        String senderName = msg.getResponseName(); //发送端的昵称
        String message = msg.getResponseMsg(); //发送端的消息
        long groupId = msg.getGroupId(); //发送端的群id
        long sendTime = msg.getSendTime(); //发送时间
        String transit = msg.getTransit(); //发送的中转地址
        long messageId = msg.getResponseMsgId(); //发送的消息的id

        // 是单聊, 在网关当作客户端时，是已经由im服务器转发过来的了，直接发送给客户端sdk
        if(type == Constants.SINGLE_CHAT) {

            // 获取连接到该网关的客户端管道
            ClientCache clientCache = SpringBeanFactory.getBean(ClientCache.class);
            NioSocketChannel clientChannel = clientCache.getClientChannel(destination);
            ResponseProtocol.Response response = ResponseProtocol.Response.newBuilder()
                    .setResponseId(senderId)
                    .setResponseName(senderName)
                    .setResponseMsgId(messageId)
                    .setResponseMsg(message)
                    .setDestination(destination)
                    .setGroupId(-1)
                    .setType(type)
                    .setSendTime(sendTime)
                    .setTransit("")
                    .build();
            clientChannel.writeAndFlush(response);
            return ;
        }

        // 是群聊,在网关作为客户端的时候，是和私聊基本一样的
        if(type == Constants.GROUP_CHAT) {
            ClientCache clientCache = SpringBeanFactory.getBean(ClientCache.class);
            NioSocketChannel clientChannel = clientCache.getClientChannel(destination);
            ResponseProtocol.Response response = ResponseProtocol.Response.newBuilder()
                    .setResponseId(senderId)
                    .setResponseName(senderName)
                    .setResponseMsgId(messageId)
                    .setResponseMsg(message)
                    .setDestination(destination)
                    .setGroupId(-1)
                    .setType(type)
                    .setSendTime(sendTime)
                    .setTransit("")
                    .build();
            clientChannel.writeAndFlush(response);
            return ;
        }

        // 是ack,发送到im服务进行处理,im服务器需要使用这个来更新,设计上网关作为客户端不可能收到ack
        if(type == Constants.ACK) {
            return ;
        }
    }

}
