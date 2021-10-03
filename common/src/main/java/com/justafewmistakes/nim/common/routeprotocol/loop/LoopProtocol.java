package com.justafewmistakes.nim.common.routeprotocol.loop;

import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IMException;
import com.justafewmistakes.nim.common.routeprotocol.RouteHandler;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class LoopProtocol implements RouteHandler {

    private final AtomicInteger index = new AtomicInteger();

    @Override
    public String selectGateway(List<String> gatewayList) {
        if(gatewayList.size() == 0) throw new IMException(FailEnums.GATEWAY_NOT_FOUND);
        int idx = index.incrementAndGet() % gatewayList.size();
        assert idx >= 0:"选择的网关序号不可为负数";
        return gatewayList.get(idx);
    }

    @Override
    public NioSocketChannel selectIMServer(List<NioSocketChannel> imServerList) {
        if(imServerList.size() == 0) throw new IMException(FailEnums.SERVER_NOT_FOUND);
        int idx = index.incrementAndGet() % imServerList.size();
        assert idx >= 0:"选择的IM服务器序号不可为负数";
        return imServerList.get(idx);
    }
}
