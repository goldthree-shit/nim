package com.justafewmistakes.nim.gateway.netty.server;

import com.justafewmistakes.nim.gateway.cache.ClientCache;
import com.justafewmistakes.nim.gateway.config.AppConfiguration;
import com.justafewmistakes.nim.gateway.netty.client.GatewayClient;
import com.justafewmistakes.nim.gateway.netty.handler.GatewayServerHandler;
import com.justafewmistakes.nim.gateway.netty.init.GatewayServerChannelHandleInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Duty: 网关作为连接客户端sdk的时候，是一个netty服务端
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class GatewayServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayServer.class);

    @Autowired
    private AppConfiguration appConfiguration; //配置

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup work = new NioEventLoopGroup();

//    @Value("${nim.port}")
//    private int port;

    /**
     * 启动网关服务端
     */
    @PostConstruct
    public void startServer() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(Integer.parseInt(appConfiguration.getGatewayPort())))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new GatewayServerChannelHandleInitializer());

        ChannelFuture future = bootstrap.bind().sync();
        if(future.isSuccess()) {
            LOGGER.info("网关服务端启动成功");
        }
    }

    /**
     * 销毁
     */
    @PreDestroy
    private void destroy() {
        boss.shutdownGracefully();
        work.shutdownGracefully();
        LOGGER.info("优雅关闭了网关服务端");
    }
}
