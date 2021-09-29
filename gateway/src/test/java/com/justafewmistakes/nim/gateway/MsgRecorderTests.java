package com.justafewmistakes.nim.gateway;

import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.gateway.kit.GatewayMsgRecorder;
import com.justafewmistakes.nim.gateway.netty.handler.GatewayClientHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@SpringBootTest
public class MsgRecorderTests {

    @Autowired
    private GatewayMsgRecorder gatewayMsgRecorder;

    @Autowired
    private GatewayClientHandler gatewayClientHandler;

    @Test
    public void testSearchOfflineFileName() {
        gatewayMsgRecorder.record(Constants.OFFLINE_MSG_PREFIX + "1", "tes1111");
        List<String> list = gatewayMsgRecorder.searchOfflineFileName("1");
        System.out.println(list);
    }
}
