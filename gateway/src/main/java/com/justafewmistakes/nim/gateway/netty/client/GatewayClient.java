package com.justafewmistakes.nim.gateway.netty.client;

import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.protobuf.RequestProtocol;
import com.justafewmistakes.nim.common.util.NtpUtil;
import com.justafewmistakes.nim.common.util.PrefixUtil;
import com.justafewmistakes.nim.gateway.cache.IMServerCache;
import com.justafewmistakes.nim.gateway.config.AppConfiguration;
import com.justafewmistakes.nim.gateway.kit.NacosClient;
import com.justafewmistakes.nim.gateway.netty.init.GatewayClientChannelHandleInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Duty:网关作为连接客户端sdk的时候，是一个netty服务端
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class GatewayClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayClient.class);

    private EventLoopGroup group = new NioEventLoopGroup(0, new DefaultThreadFactory("nim-work"));

    @Autowired
    private AppConfiguration appConfiguration; //该网关的所有配置信息

    @Autowired
    private IMServerCache imServerCache; //网关和所有的IM服务器的连接缓存

    @Autowired
    private NacosClient nacosClient; //nacos的客户端，用于获取nacos上的数据

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor; //线程池，用于并发执行连接任务

    /**
     * 获取服务端监听的地址，并且启动客户端连接上所有的地址
     */
    @PostConstruct
    public void startClient() throws ExecutionException, InterruptedException {
        int times = 0;
        // 获取全部的IM服务器的监听地址
        List<String> allIMServerAddr = getAllIMServer();

        // 启动客户端，并且连接上所有的服务器，无法完全连接，则无限循环
        while(!connectToAll(allIMServerAddr)) {
            LOGGER.error("连接所有IM服务器失败的次数[{}],进行无限重试",++times);
            allIMServerAddr = getAllIMServer();
        };

        // 向服务器发送一个包确认一下
        successConnect();

    }

    /**
     * 在连接成功后，向服务器发送一个信息确认可达,并将自己注册到服务器的缓存中
     */
    private void successConnect() {
        Map<String, NioSocketChannel> allChannelFromCache = imServerCache.getAllChannelFromCacheAsMap();
        for(Map.Entry<String, NioSocketChannel> channel : allChannelFromCache.entrySet()) {
            RequestProtocol.Request claim = RequestProtocol.Request.newBuilder()
                    .setGroupId(-1)
                    .setDestination(-1) //-1
                    .setRequestName("")
                    .setTransit("") //确认连接要让IM服务器也记录管道消息
                    .setRequestId(-1)
                    .setSendTime(NtpUtil.getNtpTime())
                    .setType(Constants.REQUEST_FOR_CONNECT)
                    .setRequestMsg(appConfiguration.getGatewayName())
                    .build();
            ChannelFuture future = channel.getValue().writeAndFlush(claim);
            future.addListener(channelFuture -> LOGGER.info("成功连接上该服务器" + channel.getKey()));
        }
    }

    /**
     * 连接上所有的服务器(初始化+nacos发生变化）
     * FIXME: 看看等一下启动另一个IMServer可不可以让线程池启动再使用-
     */
    public boolean connectToAll(List<String> allIMServerAddr) throws ExecutionException, InterruptedException {
//        if(threadPoolExecutor.isShutdown()) threadPoolExecutor.prestartAllCoreThreads();
        //FIXME：是否该锁有必要
        synchronized (GatewayClient.class) { //在这里上把锁，防止监听到nacos的变化的时候，和暂时未知的其他的变化监听变更一起进行，导致出错
            Bootstrap bootstrap = new Bootstrap();
//            NioEventLoopGroup group = new NioEventLoopGroup();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new GatewayClientChannelHandleInitializer()); //FIXME:暂时换成cim的
//                    .handler(new CIMClientHandleInitializer());
            List<Callable<Boolean>> list = new ArrayList<>();
            for (String IMServer : allIMServerAddr) {
                if(imServerCache.alreadyContain(IMServer)) continue;
                list.add(createConnectTask(bootstrap, IMServer));
            }
            if(list.size() == 0) return true; //没有网关
            List<Future<Boolean>> futures = threadPoolExecutor.invokeAll(list);
            for(Future<Boolean> now : futures) {
                if(!now.get()) {
                    //TODO: 连接失败的处理(我的想法目前是全部再重连一次)
                    return false;
                }
            }
            return true;
//            //TODO: 超时如何重试(我的想法目前也是全部再重连一次)
//            threadPoolExecutor.shutdown();
//            return threadPoolExecutor.awaitTermination(1, TimeUnit.MINUTES);
        }
    }

    /**
     * 连接上IM服务器，并将连接信息加入im服务器缓存
     */
    private Callable<Boolean> createConnectTask(Bootstrap bootstrap, String imServer) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                String[] addr = imServer.split(":");
                String ip = addr[0], port = addr[1];
                int time = 0;
                while (true) {
                    ChannelFuture future = null;
                    try {
                        future = bootstrap.connect(ip, Integer.parseInt(port)).sync();
                    } catch (Exception e) {
                        LOGGER.error("第[{}]次连接[{}]服务器失败，进行重试", ++time, imServer);
                        if(time >= appConfiguration.getRetryTime()) {
                            LOGGER.error("尝试连接该服务器[{}]次数达到上限", imServer);
                            return false;
                        }
                    }
                    if(future != null && future.isSuccess()) {
                        LOGGER.info("连接上[{}]服务器成功",imServer);
                        imServerCache.addCache(imServer, (NioSocketChannel) future.channel());
                        return true;
                    }
                }
            }
        };
    }


    /**
     * 获取所有的IM服务端的ip+port地址
     */
    private List<String> getAllIMServer() {
        List<String> preServerList = nacosClient.getAllServersInNacos().get("server:");
        List<String> serverList = new ArrayList<>();
        if(preServerList != null) {
            for(String preServer : preServerList) {
                serverList.add(PrefixUtil.parsePreGatewayToGateWay(preServer));
            }
        }
        return serverList;
    }

}
