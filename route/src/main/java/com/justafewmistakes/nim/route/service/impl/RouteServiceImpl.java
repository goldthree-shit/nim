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
            LOGGER.error("ip???[{}]????????????[{}]????????????????????????nacos??????????????????", ip, port);
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

        // ????????????????????????
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("username", loginRequestVO.getUsername());
        }};
        List<User> users = routeMapper.selectByMap(map);
        if(users.size() == 0) {
            LOGGER.error("?????????????????????[{}]?????????",loginRequestVO.getUsername());
            throw new IMException(FailEnums.USER_NOT_EXIST);
        }

        // ???????????????????????????redis???????????????
        User user = users.get(0);
        String Key = Constants.LOGIN_PREFIX + user.getId(); //??????????????????????????????(login:??????id)
        boolean loginStatus = userInfoRedisService.checkUserLoginStatus(Key);
        String token;
        if(!loginStatus) {
            token = tokenUtil.buildJWT(user.getId().toString(), user.getUsername(), gateway);
            boolean status = userInfoRedisService.saveUserLoginStatus(Key, token);//??????token???token???????????????id??????????????????
            if(!status) {
                LOGGER.error("??????id???[{}],?????????[{}]???????????????????????????",user.getId(), user.getUsername());
                throw new IMException(FailEnums.USER_AlREADY_Login);
            }
        }
        else {
            LOGGER.error("??????id???[{}],?????????[{}]???????????????????????????",user.getId(), user.getUsername());
            throw new IMException(FailEnums.USER_AlREADY_Login);
        }
        return token;
    }

    @Override
    @Transactional
    public RegisterResponseVO register(RegisterRequestVO registerRequestVO) {
        // ????????????????????????????????????
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("username", registerRequestVO.getUsername());
        }};
        List<User> users = routeMapper.selectByMap(map);
        if(users.size() != 0) {
            LOGGER.error("?????????[{}]????????????????????????", registerRequestVO.getUsername());
            throw new IMException(FailEnums.USER_AlREADY_EXIST);
        }

        // ???????????????????????????
        User user = new User();
        user.setUsername(registerRequestVO.getUsername());
        user.setPassword(registerRequestVO.getPassword());
        user.setCreateDate(new Date());
        int insert = routeMapper.insert(user);
        if(insert != 1) {
            LOGGER.error("????????????????????????[{}]?????????????????????",registerRequestVO.getUsername());
            throw new IMException(FailEnums.FAIL);
        }

        return new RegisterResponseVO(user.getId(), user.getUsername());
    }

    @Override
    public void offline(String token) {
        //???token???????????????id
        String userId = null;
        String username = null;
        try {
            Map<String, String> map = tokenUtil.getInfoMapFromToken(token);
            userId = map.get("userId");
            username = map.get("username");
        } catch (Exception e) {
            LOGGER.error("??????token??????",e);
        }

        // ???????????????????????????
        String key = Constants.LOGIN_PREFIX + userId;
        boolean isOnline = userInfoRedisService.checkUserLoginStatus(key);
        if(!isOnline) {
            LOGGER.warn("??????[{}]?????????????????????????????????",username);
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
