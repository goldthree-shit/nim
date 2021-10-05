package com.justafewmistakes.nim.server.service.impl;

import com.justafewmistakes.nim.common.entity.MsgContent;
import com.justafewmistakes.nim.common.entity.MsgIndex;
import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IMException;
import com.justafewmistakes.nim.server.mapper.MsgContentMapper;
import com.justafewmistakes.nim.server.mapper.MsgIndexMapper;
import com.justafewmistakes.nim.server.service.MsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Duty: 用于对消息与其索引进行保存
 * FIXME:是在im服务器的东西
 * @author justafewmistakes
 * Date: 2021/10
 */
@Service
public class MsgServiceImpl implements MsgService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgServiceImpl.class);

    @Autowired
    private MsgContentMapper msgContentMapper;

    @Autowired
    private MsgIndexMapper msgIndexMapper;


    @Override
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

    @Override
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
