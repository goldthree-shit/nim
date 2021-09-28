package com.justafewmistakes.nim.route.vo.response;

import com.justafewmistakes.nim.common.api.BaseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterResponseVO extends BaseVO {
    Long id; //仅仅是返回看的，用户id，没啥用
    String username;
}
