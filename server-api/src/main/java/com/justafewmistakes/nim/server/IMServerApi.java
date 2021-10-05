package com.justafewmistakes.nim.server;

import com.justafewmistakes.nim.common.api.CommonResult;

import java.util.List;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
public interface IMServerApi {

    /**
     * 获取所有的连接上的网关
     */
    CommonResult<List<String>> getAllLinkedGateway();

    /**
     * 获取所有在nacos上的网关
     */
    CommonResult<List<String>> getAllGatewayInNacos();
}
