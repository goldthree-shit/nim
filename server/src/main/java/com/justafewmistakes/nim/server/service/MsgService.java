package com.justafewmistakes.nim.server.service;

import org.springframework.transaction.annotation.Transactional;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
public interface MsgService {

    /**
     * 将数据保存到数据库
     * @param type 消息的类型
     * @param msg 消息
     * @param sendTime 发送的时间
     * @return 返回雪花生成的消息id
     */
    Long saveMsg(int type, String msg, Long sendTime);

    /**
     * 将发送的消息存入用户管道（一个抽象的管道，因为是写扩散，每个用户都会维护一个读管道）索引
     * @param ownerId 该管道所有者的客户端id
     * @param anotherClientId 在群聊中这个就是发送端的id，在单聊中这个就是另一方的id
     * @param direction 为1说明该管道是发送方
     * @param msgId 消息的id
     * @param groupId 群组的id
     * @param sendTime 发送的时间
     * @return 返回该索引的id（没有什么用）
     */
    Long saveIndex(Long ownerId, Long anotherClientId, int direction, Long msgId, Long groupId, Long sendTime);
}
