package com.justafewmistakes.nim.common.constant;

/**
 * Duty: 一些常量，包括存储的前缀等等
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class Constants {
    // 前缀基本都是存入redis时key用的,nacos上的数据也是有前缀的
    /**
     * 登录前缀
     */
    public static final String LOGIN_PREFIX = "login:";

    /**
     * msg读取前缀
     */
    public static final String READ_PREFIX = "read:";

    /**
     * 网关前缀
     */
    public static final String GATEWAY_PREFIX = "gateway:";

    /**
     * 服务器前缀
     */
    public static final String SERVER_PREFIX = "server:";

    /**
     * 服务器前缀
     */
    public static final String ROUTE_PREFIX = "route:";

    /**
     * 有离线消息的前缀
     */
    @Deprecated
    public static final String OFFLINE_MSG_PREFIX = "offlineMsg:";

    // 请求type;
    /**
     * 确认连接请求
     */
    public static final int REQUEST_FOR_CONNECT = 1;

    /**
     * 心跳ping请求
     */
    public static final int PING = 2;

    /**
     * 心跳pong请求
     */
    public static final int PONG = 3;

    /**
     * 单聊
     */
    public static final int SINGLE_CHAT = 4;

    /**
     * 群聊
     */
    public static final int GROUP_CHAT = 5;

    /**
     * 要求推送离线消息
     */
    public static final int OFFLINE_MESSAGE_NEEDED = 6;

    /**
     * 获取到推送来的离线消息
     */
    public static final int OFFLINE_MESSAGE_BACK = 7;

    /**
     * 不存在离线消息
     */
    public static final int OFFLINE_MESSAGE_LACK = 8;

    /**
     * ack
     */
    public static final int ACK = 9;

}
