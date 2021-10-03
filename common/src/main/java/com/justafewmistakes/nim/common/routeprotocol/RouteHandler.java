package com.justafewmistakes.nim.common.routeprotocol;

import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.List;

/**
 * Duty:
 * @author justafewmistakes
 * Date: 2021/09
 */
public interface RouteHandler {
    /**
     * 通过路由协议获取选择的网关节点
     * @return 返回路由协议获取的网关节点（ip+端口）
     */
    String selectGateway(List<String> gatewayList);

    /**
     * 通过路由协议获取选择的im服务器节点
     */
    NioSocketChannel selectIMServer(List<NioSocketChannel> imServerList);
}