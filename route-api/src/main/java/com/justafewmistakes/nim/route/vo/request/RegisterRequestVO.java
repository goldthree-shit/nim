package com.justafewmistakes.nim.route.vo.request;

import com.justafewmistakes.nim.common.api.BaseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Duty: 用于注册的vo
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterRequestVO{
    String username;
    String password;
}
