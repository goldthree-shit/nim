package com.justafewmistakes.nim.common.kit;

/**
 * Duty: 对收到的消息进行监听的接口，会调用日志写入方法进行日志的写入
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Deprecated
public interface MsgListener {

    /**
     * 监听收到的消息,第一个是目的地。在服务端要记录离线消息时有用，客户端就无需使用了
     */
    void listen(String destination, String msg);
}
