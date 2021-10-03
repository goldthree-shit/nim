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
@AllArgsConstructor
@Data
@NoArgsConstructor
public class MsgContent {
    @TableId("id")
    private Long id; //消息的id
    private int type; //消息的类型
    private String msg; //消息
    private Long sendTime; //发送的时间
}
