package com.justafewmistakes.nim.common.util;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.net.InetAddress;

/**
 * Duty:ntp网络时间工具类
 *
 * @author justafewmistakes
 * Date: 2021/10
 */
public class NtpUtil {

    /**
     * 获取ntp网络时间
     */
    public static Long getNtpTime() {
        try {
            NTPUDPClient timeClient = new NTPUDPClient();

            InetAddress timeServerAddress = InetAddress.getByName("pool.ntp.org");

            TimeInfo timeInfo = timeClient.getTime(timeServerAddress);

            TimeStamp timeStamp = timeInfo.getMessage().getTransmitTimeStamp();

            return timeStamp.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }
}
