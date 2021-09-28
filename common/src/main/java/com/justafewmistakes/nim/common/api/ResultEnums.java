package com.justafewmistakes.nim.common.api;

import com.justafewmistakes.nim.common.excpetion.IErrorCode;

/**
 * 枚举了一些常用API操作码
 */
public enum ResultEnums implements IErrorCode {
    SUCCESS(200, "操作成功");
    private long code;
    private String message;

    private ResultEnums(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
