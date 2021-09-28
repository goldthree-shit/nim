package com.justafewmistakes.nim.route.service.impl;

import com.justafewmistakes.nim.common.constant.Constants;
import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IMException;
import com.justafewmistakes.nim.common.util.TokenUtil;
import com.justafewmistakes.nim.route.service.UserInfoRedisService;
import com.justafewmistakes.nim.route.vo.response.UserGatewayResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Service
public class UserInfoRedisServiceImpl implements UserInfoRedisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInfoRedisServiceImpl.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TokenUtil tokenUtil;

    @Override
    public Map<Long, UserGatewayResponseVO> loadAllOnlineUser() {
        HashMap<Long, UserGatewayResponseVO> map = new HashMap<>();

        // 用cursor scan通过前缀key获取查找对象的key
        RedisConnection connection = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();
        ScanOptions scanOptions = ScanOptions.scanOptions().match(Constants.LOGIN_PREFIX + "*").build();
        Cursor<byte[]> cursor = connection.scan(scanOptions);

        // 将每个id对应的数据加入到map中
        while (cursor.hasNext()) {
            byte[] next = cursor.next();
            String key = new String(next, StandardCharsets.UTF_8); //用utf-8的，用户登入的key（login:用户id）
            String token = redisTemplate.opsForValue().get(key);
            try {
                Map<String, String> tokenMap = tokenUtil.getInfoMapFromToken(token);
                Long id = Long.parseLong(tokenMap.get("userId"));
                String username = tokenMap.get("username");
                String gateway = tokenMap.get("gateway");
                map.put(id, new UserGatewayResponseVO(id, username, gateway));
            } catch (Exception e) {
                LOGGER.error("在解析token时产生错误");
                e.printStackTrace();
            }
        }

        try {
            cursor.close();
        } catch (IOException e) {
            LOGGER.error("cursor关闭异常");
            e.printStackTrace();
        }

        return map;
    }

    @Override
    public UserGatewayResponseVO getOnlineUser(Long id) {
        String key = Constants.LOGIN_PREFIX + id;
        if(!checkUserLoginStatus(key)) {
            LOGGER.error("希望获取的id为[{}]的用户不在线",id);
            throw new IMException(FailEnums.USER_NOT_Login);
        }
        String token = redisTemplate.opsForValue().get(key);
        UserGatewayResponseVO userGatewayResponseVO = new UserGatewayResponseVO();
        try {
            Map<String, String> tokenMap = tokenUtil.getInfoMapFromToken(token);
            userGatewayResponseVO.setId(Long.parseLong(tokenMap.get("userId")));
            userGatewayResponseVO.setUsername(tokenMap.get("username"));
            userGatewayResponseVO.setGateway(tokenMap.get("gateway"));
        } catch (Exception e) {
            LOGGER.error("在解析token时产生错误");
            e.printStackTrace();
        }
        return userGatewayResponseVO;
    }

    @Override
    public boolean saveUserLoginStatus(String key, String value) {
        Boolean absent = redisTemplate.opsForValue().setIfAbsent(key, value);
        return absent == null || absent;
    }

    @Override
    public void removeUserOnlineStatus(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean checkUserLoginStatus(String key) {
        return redisTemplate.opsForValue().get(key) != null;
    }
}
