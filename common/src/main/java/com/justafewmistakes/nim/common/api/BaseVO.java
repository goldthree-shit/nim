package com.justafewmistakes.nim.common.api;

import java.io.Serializable;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class BaseVO implements Serializable {
    private final int timeStamp; //时间戳

    public BaseVO() {
        this.timeStamp = (int)(System.currentTimeMillis() / 1000);
    }

    @Override
    public String toString() {
        return "BaseVO{" +
                "timeStamp=" + timeStamp +
                '}';
    }
}
