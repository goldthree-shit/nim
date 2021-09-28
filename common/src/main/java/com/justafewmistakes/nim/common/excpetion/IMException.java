package com.justafewmistakes.nim.common.excpetion;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class IMException extends RuntimeException{
    private long code;
    private String message;

    public IMException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public IMException(FailEnums httpEnum) {
        this.code = httpEnum.getCode();
        this.message = httpEnum.getMessage();
    }

    public String getMessage() {
        return message;
    }

    public long getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
