package com.justafewmistakes.nim.common.util;

import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.protobuf.RequestProtocol;
import com.justafewmistakes.nim.common.protobuf.ResponseProtocol;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Duty: 离线消息创建和解析，主要就是在msg前面加上那一串东西,与 拆解那一串东西，然后将其封装为request包发送回去，也封装对离线消息的请求
 * 单聊：""#uid#uname#msg
 * 群聊：群id#uid#uname#msg
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Deprecated
public class OfflineMsgUtil {

    /**
     * 向消息加前缀，用于离线数据的存储
     */
    public String addPrefix(Long groupId, Long senderId, String senderName, Long sendTime, String msg) {
        return groupId + ":" + senderId + "#" + senderName + "#" + sendTime + "#" + msg;
    }

    /**
     * 将离线数据的前缀解析咯
     */
    private Map<String, String> removePrefix(String preMsg) {
        String[] pre = preMsg.split("#");
        Map<String, String> map = new HashMap<>();
        map.put("groupId", pre[0]);
        map.put("senderId", pre[1]);
        map.put("senderName", pre[2]);
        map.put("sendTime", pre[3]);
        StringBuilder sb = new StringBuilder();
        for(int i=4; i<pre.length;++i) {
            sb.append(pre[i]);
        }
        map.put("msg", sb.toString());
        return map;
    }

    /**
     * 将离线消息封住为request发送回去(在别的网关)
     * @param offlineType 是back还是lack
     * @param destination 离线消息属于的客户端id
     * @param transit 中转
     * @param preMsg 有前缀的消息
     */
    public RequestProtocol.Request getOfflineBackRequest(int offlineType, Long destination, String transit, String preMsg) {
        if(offlineType == Constants.OFFLINE_MESSAGE_LACK) return RequestProtocol.Request.newBuilder().setType(offlineType).build();
        Map<String, String> map = removePrefix(preMsg);
        return RequestProtocol.Request.newBuilder()
                .setRequestId(Long.parseLong(map.get("senderId")))
                .setRequestName(map.get("senderName"))
                .setRequestMsg(map.get("msg"))
                .setType(offlineType)
                .setDestination(destination) //离线消息属于的客户端id
                .setSendTime(Long.parseLong(map.get("sendTime")))
                .setTransit(transit)
                .setGroupId(Long.parseLong(map.get("groupId")))
                .build();
    }

    /**
     * 将离线消息封住为response发送回去(在本地连接的网关)
     * @param offlineType 是back还是lack
     * @param destination 离线消息属于的客户端id
     * @param preMsg 有前缀的消息
     * @return
     */
    public ResponseProtocol.Response getOfflineBackResponse(int offlineType, Long destination, String preMsg) {
        if(offlineType == Constants.OFFLINE_MESSAGE_LACK)
            return ResponseProtocol.Response.newBuilder().setType(offlineType).build();
        Map<String, String> map = removePrefix(preMsg);
        return ResponseProtocol.Response.newBuilder()
                .setResponseId(Long.parseLong(map.get("senderId")))
                .setResponseName(map.get("senderName"))
                .setResponseMsg(map.get("msg"))
                .setType(offlineType)
                .setDestination(destination) //离线消息属于的客户端id
                .setSendTime(Long.parseLong(map.get("sendTime")))
                .setTransit("")
                .setGroupId(Long.parseLong(map.get("groupId")))
                .build();
    }

    /**
     * 创建对离线消息的请求
     * @param senderId 发送请求离线消息的客户端id
     * @param senderName 发送请求离线消息的客户端name
     * @param gateway 存储有离线消息的网关地址
     */
    public RequestProtocol.Request createOfflineMsgRequest(Long senderId, String senderName, String gateway) {
        return RequestProtocol.Request.newBuilder()
                .setRequestId(senderId)
                .setRequestName(senderName)
                .setRequestMsg("")
                .setType(Constants.OFFLINE_MESSAGE_NEEDED)
                .setDestination(senderId)
                .setTransit(gateway)
                .setGroupId(-1)
                .setSendTime(NtpUtil.getNtpTime())
                .build();
    }
}
