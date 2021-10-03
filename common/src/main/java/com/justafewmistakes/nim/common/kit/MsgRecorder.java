package com.justafewmistakes.nim.common.kit;

/**
 * Duty: 异步缓存日志的接口
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Deprecated
public interface MsgRecorder {

    /**
     * 异步对消息进行写入
     */
    void record(String preDestination, String msg);

    /**
     * 停止对日志的写入
     */
    void stop();

    /**
     * 对日志进行查询
     */
    String search(String key);
}
