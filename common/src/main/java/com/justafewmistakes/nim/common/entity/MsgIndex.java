package com.justafewmistakes.nim.common.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsgIndex {
    @TableId("id")
    private Long id; //消息索引的id
    private Long accountA; //消息所属人的id（direction为1的时候就是发送方）
    private Long accountB; //消息另一方的id
    private int direction; //是否是发送方
    private Long msgId; //消息的id
    private Long groupId; //组的id
    private Long sendTime; //发送时间
}
