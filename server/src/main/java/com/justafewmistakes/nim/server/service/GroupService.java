package com.justafewmistakes.nim.server.service;

import java.util.List;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
public interface GroupService {

    /**
     * 获取一个群中所有的成员
     * @param groupId
     * @return
     */
    List<Long> getAllUserInGroup(Long groupId);
}
