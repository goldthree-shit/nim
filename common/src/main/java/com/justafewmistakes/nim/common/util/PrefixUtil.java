package com.justafewmistakes.nim.common.util;

/**
 * Duty:用于对前缀的操作
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class PrefixUtil {

    /**
     * 将有前缀的服务端转为无前缀
     * 例如：gateway:(ip):(im_port) -> (ip):(im_port)
     */
    public static String parsePreGatewayToGateWay(String preGateway) {
        String[] addr = preGateway.split(":"); //0是前缀gateway: ， 1是ip ，2是端口
        return addr[1] + ":" + addr[2];
    }
}
