package com.justafewmistakes.nim.common.kit;

import io.netty.channel.ChannelHandlerContext;

/**
 * Duty: (客户端、服务端)管道的 对于 过久未收到心跳的处理机制
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public interface HeartBeatHandler {

    void process(ChannelHandlerContext ctx) throws Exception;
}
