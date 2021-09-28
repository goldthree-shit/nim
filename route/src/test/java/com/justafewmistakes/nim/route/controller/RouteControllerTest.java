package com.justafewmistakes.nim.route.controller;

import com.justafewmistakes.nim.common.api.CommonResult;
import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.routeprotocol.RouteHandler;
import com.justafewmistakes.nim.route.cache.GatewayCache;
import com.justafewmistakes.nim.route.service.RouteService;
import com.justafewmistakes.nim.route.service.UserInfoRedisService;
import com.justafewmistakes.nim.route.vo.request.LoginRequestVO;
import com.justafewmistakes.nim.route.vo.request.RegisterRequestVO;
import com.justafewmistakes.nim.route.vo.response.LoginResponseVO;
import com.justafewmistakes.nim.route.vo.response.UserGatewayResponseVO;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@SpringBootTest
public class RouteControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteControllerTest.class);

    @Autowired
    private RouteController routeController;

    @Autowired
    private RouteService routeService;

    @Autowired
    private GatewayCache gatewayCache;

    @Autowired
    private RouteHandler routeHandler;

    @Autowired
    private UserInfoRedisService redisService;

    @Test
    public void reachableTest() {
        List<String> gatewayList = gatewayCache.getGatewayList(false);
        String gateway = routeHandler.selectGateway(gatewayList);
        LOGGER.info("用户名为[{}]的客户端正连接向地址为[{}]的网关", "test", gateway);
        routeService.isReachable(gateway);
    }

    @Test
    public String testLogin() {
        LoginRequestVO vo = new LoginRequestVO();
        vo.setUsername("lly");
        vo.setPassword("123");
        CommonResult<LoginResponseVO> result = routeController.login(vo);
        return result.getData().getToken();
    }

    @Test
    public void testRedisCache() {
        String key = Constants.LOGIN_PREFIX + "lly";
        boolean b = redisService.checkUserLoginStatus(key);
        LOGGER.info("现在是否已经有用户名为：[{}]的用户登录,[{}]", key, b);
    }

    @Test
    public void testRegisterAlreadyExist() {
        RegisterRequestVO registerRequestVO = new RegisterRequestVO();
        registerRequestVO.setUsername("lly");
        registerRequestVO.setPassword("123");
        routeController.register(registerRequestVO);
    }

    @Test
    public void testRegisterNotExist() {
        RegisterRequestVO registerRequestVO = new RegisterRequestVO();
        registerRequestVO.setUsername("test");
        registerRequestVO.setPassword("123");
        routeController.register(registerRequestVO);
    }

    @Test
    public void testUserOffline(String token) {
        routeController.offline(token);
    }

    @Test
    public void testLoginAndOffline() {
        String token = testLogin();
        testUserOffline(token);
    }

    @Test
    public void getAllGateway() {
        CommonResult<List<String>> gateway = routeController.getGateway();
        System.out.println(gateway);
    }

    @Test
    public void testLoadAllOnlineUser() {
        CommonResult<List<UserGatewayResponseVO>> allOnline = routeController.getAllOnline();
        System.out.println(allOnline);
    }

    @Test
    public void testLoadOnlineUser() {
        CommonResult<UserGatewayResponseVO> onlineByUserId = routeController.getOnlineByUserId(1L);
        System.out.println(onlineByUserId);
    }
}
