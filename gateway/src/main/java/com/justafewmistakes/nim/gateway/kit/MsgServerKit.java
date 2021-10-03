package com.justafewmistakes.nim.gateway.kit;

import com.justafewmistakes.nim.common.entity.MsgContent;
import com.justafewmistakes.nim.common.entity.MsgIndex;
import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IMException;
import com.justafewmistakes.nim.gateway.mapper.MsgContentMapper;
import com.justafewmistakes.nim.gateway.mapper.MsgIndexMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;

/**
 * Duty: 用于对消息与其索引进行保存
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Service
public class MsgServerKit {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgServerKit.class);

    @Autowired
    private MsgContentMapper msgContentMapper;

    @Autowired
    private MsgIndexMapper msgIndexMapper;

    /**
     * 将数据保存到数据库
     * @param type 消息的类型
     * @param msg 消息
     * @param sendTime 发送的时间
     * @return 返回雪花生成的消息id
     */
    @Transactional
    public Long saveMsg(int type, String msg, Long sendTime) {
        MsgContent content = new MsgContent();
        content.setMsg(msg);
        content.setSendTime(sendTime);
        content.setType(type);
        int insert = msgContentMapper.insert(content);
        if(insert == 0) {
            LOGGER.error("消息存入数据库失败，回滚");
            throw new IMException(FailEnums.FAIL);
        }
        return content.getId();
    }

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
    @Transactional
    public Long saveIndex(Long ownerId, Long anotherClientId, int direction, Long msgId, Long groupId, Long sendTime) {
        MsgIndex index = new MsgIndex();
        index.setAccountA(ownerId);
        index.setAccountB(anotherClientId);
        index.setDirection(direction);
        index.setMsgId(msgId);
        index.setGroupId(groupId);
        index.setSendTime(sendTime);
        int insert = msgIndexMapper.insert(index);
        if(insert == 0) {
            LOGGER.error("消息索引存入数据库失败，回滚");
            throw new IMException(FailEnums.FAIL);
        }
        return index.getId();
    }
}
