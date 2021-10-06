package com.justafewmistakes.nim.server.netty.server;

import com.justafewmistakes.nim.server.config.AppConfiguration;
import com.justafewmistakes.nim.server.netty.init.IMServerChannelHandlerInitializer;
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

/**
 * Duty:im服务器的netty服务端，用于网关的客户端连接与信息的处理
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Component
public class IMServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(IMServer.class);

    @Autowired
    private AppConfiguration appConfiguration; //配置

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup work = new NioEventLoopGroup();

//    @Value("${nim.port}")
//    private int port;

    /**
     * 启动im服务器
     */
    @PostConstruct
    public void startIMServer() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(Integer.parseInt(appConfiguration.getImServerPort())))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new IMServerChannelHandlerInitializer());  //FIXME:暂时换成cim的
//                .childHandler(new CIMServerInitializer());
        ChannelFuture future = bootstrap.bind().sync();
        if(future.isSuccess()) {
            LOGGER.info("im服务器启动成功");
        }
    }

    /**
     * 销毁
     */
    @PreDestroy
    private void destroy() {
        boss.shutdownGracefully();
        work.shutdownGracefully();
        LOGGER.info("优雅关闭了im服务器");
    }
}
