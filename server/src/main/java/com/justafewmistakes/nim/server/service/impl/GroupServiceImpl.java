package com.justafewmistakes.nim.server.service.impl;

import com.justafewmistakes.nim.common.entity.Group;
import com.justafewmistakes.nim.server.mapper.GroupMapper;
import com.justafewmistakes.nim.server.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Duty: 用于群聊拆分
 * FIXME:是在im服务器的东西
 * @author justafewmistakes
 * Date: 2021/10
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupMapper groupMapper;

    @Override
    public List<Long> getAllUserInGroup(Long groupId) {
        return groupMapper.allGroupUser(groupId);
    }
}
