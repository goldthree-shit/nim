package com.justafewmistakes.nim.gateway;

import com.justafewmistakes.nim.common.api.CommonResult;
import com.justafewmistakes.nim.gateway.vo.response.IMServerInfoResponseVO;

import java.util.List;

/**
 * Duty: 一些查看网关信息的接口
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public interface GatewayApi {

    /**
     * 获取连接到该网关的全部的客户端id
     */
    CommonResult<List<String>> getAllClientId();

    /**
     * 获取该网关连接的全部服务端信息
     */
    CommonResult<List<IMServerInfoResponseVO>> getAllServerInfo();
}
