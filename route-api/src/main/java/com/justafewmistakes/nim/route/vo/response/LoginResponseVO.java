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
public class LoginResponseVO extends BaseVO {
    String token;
    String gateway;
}
