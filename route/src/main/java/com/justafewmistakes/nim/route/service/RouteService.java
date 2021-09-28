package com.justafewmistakes.nim.route.service;

import com.justafewmistakes.nim.route.vo.request.LoginRequestVO;
import com.justafewmistakes.nim.route.vo.request.RegisterRequestVO;
import com.justafewmistakes.nim.route.vo.response.RegisterResponseVO;
import com.justafewmistakes.nim.route.vo.response.UserGatewayResponseVO;

import java.util.Map;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public interface RouteService {

    /**
     * 可达性验证
     */
    boolean isReachable(String gateway);

    /**
     * 登入并向redis和数据库保存状态(目前仅向redis中保存)
     */
    String loginAndSaveStatus(LoginRequestVO loginRequestVO, String gateway);

    /**
     * 注册
     */
    RegisterResponseVO register(RegisterRequestVO registerRequestVO);

    /**
     * 下线
     */
    void offline(String token);

    /**
     * 获取所有用户他们和网关的关系
     */
    Map<Long, UserGatewayResponseVO> loadAllUserGatewayRelation();

    /**
     * 获取单个用户和网关的关系
     */
    UserGatewayResponseVO loadUserGatewayRelation(Long userId);
}
