package com.justafewmistakes.nim.route.mapper;

import com.justafewmistakes.nim.route.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@SpringBootTest
public class RouteMapperTest {
    @Autowired
    private RouteMapper routeMapper;

    @Test
    public void addToMysqlTest() {
        routeMapper.insert(new User(1l, "lly", "123", 0,new Date()));
    }

    @Test
    public void getUserTest() {
        User user = routeMapper.selectById(1);
        System.out.println(user);
        System.out.println(new SimpleDateFormat("yyyy-MM-dd").format(user.getCreateDate()));
    }
}
