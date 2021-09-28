package com.justafewmistakes.nim.route;

import com.justafewmistakes.nim.common.api.CommonResult;
import com.justafewmistakes.nim.route.vo.request.LoginRequestVO;
import com.justafewmistakes.nim.route.vo.request.RegisterRequestVO;
import com.justafewmistakes.nim.route.vo.response.LoginResponseVO;
import com.justafewmistakes.nim.route.vo.response.RegisterResponseVO;
import com.justafewmistakes.nim.route.vo.response.UserGatewayResponseVO;

import java.util.List;

/**
 * Duty: 路由模块暴露出去，为了让客户端SDK使用的接口
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public interface RouteApi {

    /**
     * 登入接口，登录后，调用获取网关的方法，获取可用网关并返回
     * 如果是已经登入的状态，就抛出
     */
    CommonResult<LoginResponseVO> login(LoginRequestVO info);

    /**
     * 注册的接口，讲用户信息注册到mysql中
     */
    CommonResult<RegisterResponseVO> register(RegisterRequestVO info);

    /**
     * 下线接口，用于用户主动下线，或者用户管道断开，并且自动连接失败时调用
     */
    CommonResult<String> offline(String token);

    /**
     * 根据网关选择协议，获取可用的网关
     */
    CommonResult<List<String>> getGateway();

    /**
     * 获取全部在线的用户
     */
    CommonResult<List<UserGatewayResponseVO>> getAllOnline();

    /**
     * 获取用户id对应的在线人
     */
    CommonResult<UserGatewayResponseVO> getOnlineByUserId(Long userId);
}
