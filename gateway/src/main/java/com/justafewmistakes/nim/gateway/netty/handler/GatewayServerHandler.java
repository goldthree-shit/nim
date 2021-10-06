package com.justafewmistakes.nim.gateway.netty.handler;

import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IMException;
import com.justafewmistakes.nim.common.kit.HeartBeatHandler;
import com.justafewmistakes.nim.common.protobuf.RequestProtocol;
import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import com.justafewmistakes.nim.common.routeprotocol.RouteHandler;
import com.justafewmistakes.nim.common.util.HearBeatUtil;
import com.justafewmistakes.nim.gateway.cache.ClientCache;
import com.justafewmistakes.nim.gateway.cache.IMServerCache;
import com.justafewmistakes.nim.gateway.kit.SpringBeanFactory;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Duty: 网关作为服务端的自定义管道处理器
 * TODO: 将3个handler的属性设置一下
 * @author justafewmistakes
 * Date: 2021/09
 */
//@ChannelHandler.Sharable
public class GatewayServerHandler extends SimpleChannelInboundHandler<ResponseProtocol.Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayServerHandler.class);

//    private final IMServerCache imServerCache = SpringBeanFactory.getBean(IMServerCache.class); //获取所有的im服务器

//    private final RouteHandler routeHandler = SpringBeanFactory.getBean(RouteHandler.class); //（缓存im服务器和网关的关系）选择im服务器

//    private final ClientCache clientCache = SpringBeanFactory.getBean(ClientCache.class); //（缓存客户端和网关的关系）获取转发网关,这个功能可能用不上了

//    private final HearBeatUtil hearBeatUtil = SpringBeanFactory.getBean(HearBeatUtil.class); //用于快速创建心跳包

//    private final HeartBeatHandler heartBeatHandler = SpringBeanFactory.getBean(HeartBeatHandler.class); //网关服务端的心跳处理器，用于多次读空闲后断开连接

    /**
     * 管道失效
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 可能会读空闲过久后已经下线了后触发，但是不管，也直接让他下线
        ClientCache clientCache = SpringBeanFactory.getBean(ClientCache.class);
        Long clientId = clientCache.clientOffline((NioSocketChannel) ctx.channel());

        if(clientId != null) {
            LOGGER.warn("客户端[{}]管道断开，清除缓存,并让用户下线",clientId);
            //FIXME:此时连接关闭了吗 等一下验证完加上if(ctx.channel().isActive())
            ctx.channel().close();
        }

        super.channelInactive(ctx);
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

                HeartBeatHandler heartBeatHandler = SpringBeanFactory.getBean(HeartBeatHandler.class);
                heartBeatHandler.process(ctx);
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    /**
     * 管道首次活跃
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO:看看这个是不是能正常获取名字（这个是顺序问题）
        if(ctx.channel() instanceof NioSocketChannel) {
            ClientCache clientCache = SpringBeanFactory.getBean(ClientCache.class);
            LOGGER.info("客户端" + clientCache.getClientId((NioSocketChannel) ctx.channel()) + "连接到网关服务端成功,管道首次活跃触发");
        }
        super.channelActive(ctx);
    }

    /**
     * 读取到数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseProtocol.Response msg) throws Exception {

        int type = msg.getType();

        LOGGER.info("网关客户端收到消息"+msg.toString());

        if(type == Constants.PONG) return;
        if(type == Constants.PING) {
            HearBeatUtil hearBeatUtil = SpringBeanFactory.getBean(HearBeatUtil.class);
            ChannelFuture future = ctx.writeAndFlush(hearBeatUtil.Pong());

            future.addListener((ChannelFutureListener) future0 -> {
                if(!future0.isSuccess()) {
                    LOGGER.error("PING 发送失败，io错误，关闭channel");
                    future0.channel().close(); //TODO: 1.看看会不会进inactive方法
                }
            });
        }

        String transit = msg.getTransit(); //发送的中转地址（不需要了）
        long senderId = msg.getResponseId(); //即为发送端的id
        long destination = msg.getDestination(); //即为接收端的id，发群消息的时候，这个在网关服务端是没有的，会在im服务器中被填充
        String senderName = msg.getResponseName(); //发送端的昵称
        String message = msg.getResponseMsg(); //发送端的消息
        long groupId = msg.getGroupId(); //发送端的群id
        long sendTime = msg.getSendTime(); //发送时间
        long messageId = msg.getResponseMsgId(); //发送的消息的id，这个在网关服务端是没有的，会在im服务器中被填充【如果是要求获取离线消息的时候，会有】

        // 客户端连接上网关（同步完离线消息才会连接）
        if(type == Constants.REQUEST_FOR_CONNECT) {
            ClientCache clientCache = SpringBeanFactory.getBean(ClientCache.class);
            clientCache.addCache(senderId, (NioSocketChannel) ctx.channel());

            LOGGER.info("客户端[id:{}][name:{}]连接上服务器，并且注册到缓存中成功",senderId, senderName);
        }

        // 获取发送到的IM服务器管道
        IMServerCache imServerCache = SpringBeanFactory.getBean(IMServerCache.class);
        List<NioSocketChannel> list = imServerCache.getAllChannelFromCacheAsList();

        RouteHandler routeHandler = SpringBeanFactory.getBean(RouteHandler.class);
        NioSocketChannel channel = routeHandler.selectIMServer(list);

        // 客户端刚刚上线的时候，用他自己已经读取的最后的msgId来向im服务器要求后续的离线消息 TODO：im服务器要配置一次最多回传几条,然后客户端会要求多次，直到回传type为lack而不是back
        // ack要转发到im服务器，记录该用户的已经读取的消息id TODO：im服务器中要向redis中加入这个东西记录以及读取的消息id，然后每次上线都去服务端要求读取，还有冷启动问题
        // 单聊和群聊处理，直接转发到服务端
        if(type == Constants.SINGLE_CHAT || type == Constants.GROUP_CHAT
            || type == Constants.OFFLINE_MESSAGE_NEEDED || type == Constants.ACK) {
//            // 获取要转发到的网关(不需要了)
//            String transitGateway = clientCache.getOnlineClientGatewayInfo(destination);

            RequestProtocol.Request request = RequestProtocol.Request.newBuilder()
                    .setRequestId(senderId)
                    .setType(type)
                    .setGroupId(groupId)
                    .setSendTime(sendTime)
                    .setRequestMsg(message)
                    .setTransit("")
                    .setRequestName(senderName)
                    .setDestination(destination)
                    .setRequestMsgId(messageId)
                    .build();

            ChannelFuture future = channel.writeAndFlush(request);
            future.addListener((ChannelFutureListener) future0 -> {
                //TODO:发送失败处理（暂定）
               LOGGER.error("[{}(4单聊,5群聊,6申请离线消息,9ack)]发送到im服务器失败，io异常，暂时处理方案是换个管道继续发送,至多3次",type);
               int time = 0;
               while(!resent(request) && time < 3) {
                   ++time;
                   if(time == 3) throw new IMException(FailEnums.FAIL);
               }
            });
        }
    }

    /**
     * 发送消息到im服务器，失败的重发
     * @param request request的内容
     */
    private boolean resent(RequestProtocol.Request request) {
        // 获取发送到的IM服务器管道
        IMServerCache imServerCache = SpringBeanFactory.getBean(IMServerCache.class);
        List<NioSocketChannel> list = imServerCache.getAllChannelFromCacheAsList();

        RouteHandler routeHandler = SpringBeanFactory.getBean(RouteHandler.class);
        NioSocketChannel channel = routeHandler.selectIMServer(list);
        AtomicBoolean state = new AtomicBoolean(true);
        channel.writeAndFlush(request).addListener(future -> {
            if(!future.isSuccess()) state.set(false);
        });
        return state.get();
    }
}
