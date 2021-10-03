package com.justafewmistakes.nim.common.entity;

import com.baomidou.mybatisplus.annotation.TableId;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
public class GroupUser {
    @TableId("id")
    private Long id;
    private Long groupId;
    private Long userId;
}
