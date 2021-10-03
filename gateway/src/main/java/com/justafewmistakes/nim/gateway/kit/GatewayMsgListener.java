package com.justafewmistakes.nim.gateway.kit;

import com.justafewmistakes.nim.common.kit.MsgListener;
import com.justafewmistakes.nim.common.kit.MsgRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Duty: 网关消息监听
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
@Deprecated
public class GatewayMsgListener implements MsgListener {

    @Autowired
    private MsgRecorder msgRecorder;

    @Override
    public void listen(String preDestination, String preMsg) {
        msgRecorder.record(preDestination, preMsg);
    }
}
