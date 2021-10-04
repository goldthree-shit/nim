package com.justafewmistakes.nim.gateway.controller;

import com.justafewmistakes.nim.common.api.CommonResult;
import com.justafewmistakes.nim.gateway.GatewayApi;
import com.justafewmistakes.nim.gateway.vo.response.IMServerInfoResponseVO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@RestController
@RequestMapping("/gateway")
public class GatewayController implements GatewayApi {
    @Override
    public CommonResult<List<String>> getAllClientId() {
        return null;
    }

    @Override
    public CommonResult<List<IMServerInfoResponseVO>> getAllServerInfo() {
        return null;
    }
}
