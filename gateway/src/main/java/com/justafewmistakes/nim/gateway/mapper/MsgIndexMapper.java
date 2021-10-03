package com.justafewmistakes.nim.gateway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.justafewmistakes.nim.common.entity.MsgIndex;
import org.apache.ibatis.annotations.Mapper;

/**
 * Duty:对消息索引操作的mapper
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Mapper
public interface MsgIndexMapper extends BaseMapper<MsgIndex> {
}
