package com.justafewmistakes.nim.gateway;

import com.justafewmistakes.nim.common.util.NtpUtil;
import com.justafewmistakes.nim.gateway.kit.GroupServerKit;
import com.justafewmistakes.nim.gateway.kit.MsgServerKit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GatewayApplicationTests {

    @Autowired
    private GroupServerKit groupServerKit;

    @Autowired
    private MsgServerKit msgServerKit;

    @Test
    void testGetGroupUser() {
        List<Long> allUserInGroup = groupServerKit.getAllUserInGroup(1L);
        System.out.println(allUserInGroup);
    }

    @Test
    void testSaveMsg() {
        Long id = msgServerKit.saveMsg(1, "aa", NtpUtil.getNtpTime());
        System.out.println(id);
    }

    @Test
    void testSaveMsgIndex() {
        Long index = msgServerKit.saveIndex(1L, 2L, 1, 123L, 0L, NtpUtil.getNtpTime());
        System.out.println(index);
    }

}
