package com.justafewmistakes.nim.route.service.impl;

import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IMException;
import com.justafewmistakes.nim.common.util.TokenUtil;
import com.justafewmistakes.nim.route.cache.GatewayCache;
import com.justafewmistakes.nim.common.entity.User;
import com.justafewmistakes.nim.route.kit.NacosClient;
import com.justafewmistakes.nim.route.mapper.RouteMapper;
import com.justafewmistakes.nim.route.service.RouteService;
import com.justafewmistakes.nim.route.service.UserInfoRedisService;
import com.justafewmistakes.nim.route.vo.request.LoginRequestVO;
import com.justafewmistakes.nim.route.vo.request.RegisterRequestVO;
import com.justafewmistakes.nim.route.vo.response.RegisterResponseVO;
import com.justafewmistakes.nim.route.vo.response.UserGatewayResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Service
public class RouteServiceImpl implements RouteService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteServiceImpl.class);

    @Autowired
    private TokenUtil tokenUtil;

    @Autowired
    private GatewayCache gatewayCache;

    @Autowired
    private NacosClient nacosClient;

    @Autowired
    private RouteMapper routeMapper;

    @Autowired
    private UserInfoRedisService userInfoRedisService;

    @Override
    public boolean isReachable(String gateway) {
        String[] addr = gateway.split(":");
        String ip = addr[0], port = addr[1];
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)), 1000);
            return true;
        } catch (IOException e) {
            LOGGER.error("ip为[{}]，端口为[{}]的地址不可用，从nacos更新新的缓存", ip, port);
            gatewayCache.updateCache(nacosClient.getAllServersInNacos().get("gateway:"));
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public String loginAndSaveStatus(LoginRequestVO loginRequestVO, String gateway) {

        // 检查用户是否存在
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("username", loginRequestVO.getUsername());
        }};
        List<User> users = routeMapper.selectByMap(map);
        if(users.size() == 0) {
            LOGGER.error("希望登入的用户[{}]不存在",loginRequestVO.getUsername());
            throw new IMException(FailEnums.USER_NOT_EXIST);
        }

        // 获取用户键进行尝试redis中注册登入
        User user = users.get(0);
        String Key = Constants.LOGIN_PREFIX + user.getId(); //用作已经登录状态的键(login:用户id)
        boolean loginStatus = userInfoRedisService.checkUserLoginStatus(Key);
        String token;
        if(!loginStatus) {
            token = tokenUtil.buildJWT(user.getId().toString(), user.getUsername(), gateway);
            boolean status = userInfoRedisService.saveUserLoginStatus(Key, token);//保存token，token中有用户的id和连接的网关
            if(!status) {
                LOGGER.error("用户id为[{}],昵称为[{}]的用户已经登入过了",user.getId(), user.getUsername());
                throw new IMException(FailEnums.USER_AlREADY_Login);
            }
        }
        else {
            LOGGER.error("用户id为[{}],昵称为[{}]的用户已经登入过了",user.getId(), user.getUsername());
            throw new IMException(FailEnums.USER_AlREADY_Login);
        }
        return token;
    }

    @Override
    @Transactional
    public RegisterResponseVO register(RegisterRequestVO registerRequestVO) {
        // 检查是否已经存在该用户了
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("username", registerRequestVO.getUsername());
        }};
        List<User> users = routeMapper.selectByMap(map);
        if(users.size() != 0) {
            LOGGER.error("昵称为[{}]的用户已经存在了", registerRequestVO.getUsername());
            throw new IMException(FailEnums.USER_AlREADY_EXIST);
        }

        // 向数据表中插入用户
        User user = new User();
        user.setUsername(registerRequestVO.getUsername());
        user.setPassword(registerRequestVO.getPassword());
        user.setCreateDate(new Date());
        int insert = routeMapper.insert(user);
        if(insert != 1) {
            LOGGER.error("数据库插入新用户[{}]失败，原因未知",registerRequestVO.getUsername());
            throw new IMException(FailEnums.FAIL);
        }

        return new RegisterResponseVO(user.getId(), user.getUsername());
    }

    @Override
    public void offline(String token) {
        //从token中获取用户id
        String userId = null;
        String username = null;
        try {
            Map<String, String> map = tokenUtil.getInfoMapFromToken(token);
            userId = map.get("userId");
            username = map.get("username");
        } catch (Exception e) {
            LOGGER.error("解析token异常");
            e.printStackTrace();
        }

        // 使用键检查登入状态
        String key = Constants.LOGIN_PREFIX + userId;
        boolean isOnline = userInfoRedisService.checkUserLoginStatus(key);
        if(!isOnline) {
            LOGGER.warn("用户[{}]已经下线，无需再次下线",username);
            return;
        }
        userInfoRedisService.removeUserOnlineStatus(key);
    }

    @Override
    public Map<Long, UserGatewayResponseVO> loadAllUserGatewayRelation() {
        return userInfoRedisService.loadAllOnlineUser();
    }

    @Override
    public UserGatewayResponseVO loadUserGatewayRelation(Long userId) {
        return userInfoRedisService.getOnlineUser(userId);
    }
}
