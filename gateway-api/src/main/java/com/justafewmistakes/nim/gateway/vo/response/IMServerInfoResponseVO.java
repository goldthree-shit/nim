package com.justafewmistakes.nim.gateway.vo.response;

/**
 * Duty: 网关连接的服务端的信息
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class IMServerInfoResponseVO {
    Long gid; //网关的id
    Long sid; //服务端id
    String server; //服务端的ip+port
    String gateway; //网关的ip+port
}
