package com.justafewmistakes.nim.route.controller;

import com.justafewmistakes.nim.common.api.CommonResult;
import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IMException;
import com.justafewmistakes.nim.common.routeprotocol.RouteHandler;
import com.justafewmistakes.nim.route.RouteApi;
import com.justafewmistakes.nim.route.cache.GatewayCache;
import com.justafewmistakes.nim.route.service.RouteService;
import com.justafewmistakes.nim.route.vo.request.LoginRequestVO;
import com.justafewmistakes.nim.route.vo.request.RegisterRequestVO;
import com.justafewmistakes.nim.route.vo.response.LoginResponseVO;
import com.justafewmistakes.nim.route.vo.response.RegisterResponseVO;
import com.justafewmistakes.nim.route.vo.response.UserGatewayResponseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */

@Api("路由控制器")
@RestController
@RequestMapping("/route")
public class RouteController implements RouteApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteController.class);

    @Autowired
    private GatewayCache gatewayCache;

    @Autowired
    private RouteHandler routeHandler;

    @Autowired
    private RouteService routeService;

    @ApiOperation("登入接口")
    @PostMapping("/login")
    @Override
    public CommonResult<LoginResponseVO> login(LoginRequestVO info) {

        // 获取网关，并且检验是否可以使用，不可用从nacos强制刷新获取
        String gateway;
        int time = 0;
        do { // 最多进行3次
            if(time++ > 2) {
                throw new IMException(FailEnums.GATEWAY_NOT_FOUND);
            }
            List<String> gatewayList = gatewayCache.getGatewayList(false);
            gateway = routeHandler.selectGateway(gatewayList);
            LOGGER.info("用户名为[{}]的客户端正连接向地址为[{}]的网关", info.getUsername(), gateway);
        }
        while (!routeService.isReachable(gateway));

        // 获取希望登入的用户的token（用户名+网关地址），如果已经登入了则抛出异常
        String token = routeService.loginAndSaveStatus(info, gateway);

        LoginResponseVO loginResponseVO = new LoginResponseVO();
        loginResponseVO.setGateway(gateway);
        loginResponseVO.setToken(token);
        return CommonResult.success(loginResponseVO);
    }

    @ApiOperation("注册接口")
    @PostMapping("/register")
    @Override
    public CommonResult<RegisterResponseVO> register(RegisterRequestVO info) {

        // 路由服务层检查是否已经存在该用户了,存在则抛出异常
        RegisterResponseVO responseVO = routeService.register(info);

        return CommonResult.success(responseVO);
    }

    @ApiOperation("下线接口")
    @PostMapping("/offline")
    @Override
    public CommonResult<String> offline(String token) {

        // 进行下线操作，如果已经下线则打印日志，但是不抛异常
        routeService.offline(token);

        return CommonResult.success("下线成功");
    }

    @ApiOperation("获取全部网关接口")
    @GetMapping("/gateway")
    @Override
    public CommonResult<List<String>> getGateway() {
        List<String> gatewayList = gatewayCache.getGatewayList(false);
        return CommonResult.success(gatewayList);
    }


    @ApiOperation("获取全部在线人")
    @GetMapping("/all")
    @Override
    public CommonResult<List<UserGatewayResponseVO>> getAllOnline() {

        Map<Long, UserGatewayResponseVO> map = routeService.loadAllUserGatewayRelation();
        ArrayList<UserGatewayResponseVO> list = new ArrayList<>();
        for(Map.Entry<Long, UserGatewayResponseVO> entry : map.entrySet()) {
            list.add(entry.getValue());
        }
        return CommonResult.success(list);
    }

    @ApiOperation("获取用户id对应的在线人")
    @GetMapping("/online/{id}")
    @Override
    public CommonResult<UserGatewayResponseVO> getOnlineByUserId(@PathVariable("id") Long userId) {

        UserGatewayResponseVO userGatewayResponseVO = routeService.loadUserGatewayRelation(userId);

        return CommonResult.success(userGatewayResponseVO);
    }
}
