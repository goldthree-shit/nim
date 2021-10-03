package com.justafewmistakes.nim.gateway.kit;

import com.justafewmistakes.nim.common.entity.Group;
import com.justafewmistakes.nim.gateway.mapper.GroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Duty: 用于群聊拆分
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Service
public class GroupServerKit {

    @Autowired
    private GroupMapper groupMapper;

    public List<Long> getAllUserInGroup(Long groupId) {
        return groupMapper.allGroupUser(groupId);
    }
}
