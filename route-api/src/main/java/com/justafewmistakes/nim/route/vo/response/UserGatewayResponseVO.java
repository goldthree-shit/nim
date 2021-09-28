package com.justafewmistakes.nim.route.vo.response;

import com.justafewmistakes.nim.common.api.BaseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Duty:用于返回用户和网关的对应关系
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserGatewayResponseVO extends BaseVO {
    Long id;
    String username;
    String gateway;
}
