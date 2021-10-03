package com.justafewmistakes.nim.common.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Duty: 组的信息
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    @TableId("id")
    private Long id; //群组的id
    private String groupName; //群组的名称
}
