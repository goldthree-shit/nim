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
import com.justafewmistakes.nim.gateway.kit.ReachableKit;
import com.justafewmistakes.nim.gateway.kit.RedisServerKit;
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
    private HearBeatUtil hearBeatUtil; //心跳包创建工具

    @Autowired
    private IMServerCache imServerCache; //用于管道不可用时，删除管道与获取连接向im服务器的管道

    @Autowired
    private ClientCache clientCache; //用于获取所有与客户端管道相关的东西

    @Autowired
    private AppConfiguration appConfiguration;

    @Autowired
    private RedisServerKit redisServerKit; //在这里用于获取是否有离线消息与是否在线

    @Autowired
    private ReachableKit reachableKit; //可达性分析

    @Autowired
    private RouteHandler routeHandler; //路由选择协议（用于获取im服务器）

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
    @SuppressWarnings("all")
    protected void channelRead0(ChannelHandlerContext ctx, ResponseProtocol.Response msg) throws Exception {

        //TODO: 网关作为服务端的时候（这里是客户端，灵感）,需要将信息写入数据库
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

        // 在线和可达状态
        /* 网关不管他在不在线！只进行转发操作和消息的存储操作
        boolean isOnline = redisServerKit.isOnline(destination); //是否在线
        boolean isClientReachable = reachableKit.isClientReachable(destination); //是否客户端可达（在线的时候，可能管道莫得了） TODO：要去检查是否可达,不可达怎么办呢(目前的想法是循环检测10次。然后就不管了）
        int time = 0;

        // 如果在线但不可达，循环至多10次
        while(isOnline && !isClientReachable && time<10) {
            isClientReachable = reachableKit.isClientReachable(destination);
            ++time;
        }
        */


        // 是单聊, 在网关当作客户端时，是已经由im服务器转发过来的了，直接发送给客户端sdk
        if(type == Constants.SINGLE_CHAT) {
//            if(!isOnline || !isClientReachable) {
//                String preDestination = Constants.OFFLINE_MSG_PREFIX + destination; //存入离线文件的有前缀的接受端id
//                String preMsg = offlineMsgUtil.addPrefix(groupId, senderId, senderName, sendTime, message); //存入离线文件的msg
//                gatewayMsgListener.listen(preDestination, preMsg);
//            }

            /* 这个是网关作为server的
            // 获取发送到的IM服务器管道
            List<NioSocketChannel> list = imServerCache.getAllChannelFromCacheAsList();
            NioSocketChannel channel = routeHandler.selectIMServer(list);
            // 获取要转发到的网关
            String transitGateway = clientCache.getOnlineClientGatewayInfo(destination);

            RequestProtocol.Request request = RequestProtocol.Request.newBuilder()
                    .setRequestId(senderId)
                    .setType(type)
                    .setGroupId(-1)
                    .setSendTime(sendTime)
                    .setRequestMsg(message)
                    .setTransit(transitGateway)
                    .setRequestName(senderName)
                    .setDestination(destination)
                    .build();

            channel.writeAndFlush(request);
             */
            // 获取连接到该网关的客户端管道
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

        // 是ack,发送到im服务进行处理,im服务器需要使用这个来更新
        if(type == Constants.ACK) {

        }
    }
}
