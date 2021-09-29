package com.justafewmistakes.nim.common.constant;

/**
 * Duty: 一些常量，包括存储的前缀等等
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class Constants {
    // 前缀
    public static final String LOGIN_PREFIX = "login:"; //登录前缀
    public static final String GATEWAY_PREFIX = "gateway:"; //网关前缀
    public static final String SERVER_PREFIX = "server:"; //服务器前缀
    public static final String ROUTE_PREFIX = "route:"; //服务器前缀
    public static final String OFFLINE_MSG_PREFIX = "offlineMsg:"; //TODO：有离线消息的前缀，去路由找网关的时候，首先找有离线消息存在的路由

    // 请求type
    public static final int REQUEST_FOR_CONNECT = 1; //确认连接请求
    public static final int PING = 2; //心跳ping请求
    public static final int PONG = 3; //心跳pong请求
    public static final int SINGLE_CHAT = 4; //单聊
    public static final int GROUP_CHAT = 5; //群聊
    public static final int OFFLINE_MESSAGE = 6; //要求推送离线消息



}
