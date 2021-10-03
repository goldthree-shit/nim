package com.justafewmistakes.nim.gateway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.justafewmistakes.nim.common.entity.Group;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Duty: 对群组操作的mapper
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Mapper
public interface GroupMapper extends BaseMapper<Group> {

    List<Long> allGroupUser(Long groupId);
}
