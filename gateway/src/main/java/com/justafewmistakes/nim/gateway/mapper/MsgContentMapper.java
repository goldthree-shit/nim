package com.justafewmistakes.nim.gateway.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.justafewmistakes.nim.common.entity.MsgContent;
import org.apache.ibatis.annotations.Mapper;

/**
 * Duty: 对消息内容操作的mapper
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Mapper
public interface MsgContentMapper extends BaseMapper<MsgContent> {
}
