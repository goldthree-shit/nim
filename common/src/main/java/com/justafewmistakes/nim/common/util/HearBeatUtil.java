package com.justafewmistakes.nim.common.util;

import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.protobuf.RequestProtocol;
import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Duty: 心跳工具包，用于生成心跳
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Data
@AllArgsConstructor
public class HearBeatUtil {

    /**
     * 生成心跳ping包
     */
    public RequestProtocol.Request Ping() {
        return RequestProtocol.Request.newBuilder()
                .setRequestId(-1)
                .setGroupId(-1)
                .setRequestName("")
                .setRequestMsg("PING")
                .setType(Constants.PING)
                .setDestination(-1)
                .setTransit("")
                .setSendTime(NtpUtil.getNtpTime())
                .build();
    }

    /**
     * 生成心跳pong包
     */
    public ResponseProtocol.Response Pong() {
        return ResponseProtocol.Response.newBuilder()
                .setResponseId(-1)
                .setGroupId(-1)
                .setResponseName("")
                .setResponseMsg("PONG")
                .setType(Constants.PONG)
                .setDestination(-1)
                .setTransit("")
                .setSendTime(NtpUtil.getNtpTime())
                .build();
    }
}
