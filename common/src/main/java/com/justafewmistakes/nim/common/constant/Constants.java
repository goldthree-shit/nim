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
    //TODO：有离线消息的前缀，功能实现在客户端和网关，客户端会去找目前的网关要求发送离线消息到网关的服务端(destination)，
    // 服务端会轮询出一个到服务器的管道发给IM服务器，im发给有离线消息的网关客户端(6)，网关客户端发现是要求推送离线消息，
    // 就会将离线消息通过轮询出一个im服务器发送过去，im服务器再发送给请求的网关的客户端(7)，网关客户端通过指定的管道发回去



}
