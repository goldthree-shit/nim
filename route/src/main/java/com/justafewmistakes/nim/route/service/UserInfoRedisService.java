package com.justafewmistakes.nim.route.service;

import com.justafewmistakes.nim.route.vo.response.UserGatewayResponseVO;

import java.util.List;
import java.util.Map;

/**
 * Duty:用于从redis中获取缓存的用户信息的（登入信息、连接信息等等）
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public interface UserInfoRedisService {

    /**
     * 获取所有在线人
     */
    Map<Long, UserGatewayResponseVO> loadAllOnlineUser();

    /**
     * 获取id对应的用户的登入信息
     */
    UserGatewayResponseVO getOnlineUser(Long id);

    /**
     * 保存用户的登录状态
     * key是 登入前缀+用户id
     * value是 token
     */
    boolean saveUserLoginStatus(String key, String value);

    /**
     * 清除用户登录状态
     */
    void removeUserOnlineStatus(String key);

    /**
     * 检测用户是否已经登入
     */
    boolean checkUserLoginStatus(String key);
}
