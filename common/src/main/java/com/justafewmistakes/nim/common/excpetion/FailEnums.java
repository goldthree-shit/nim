package com.justafewmistakes.nim.common.excpetion;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public enum FailEnums implements IErrorCode{

//    SUCCESS(200, "success"), //请求成功

    UNAUTHORIZED(401, "unauthorized"), //未授权

    FAIL(500, "unknown_error"), //未知错误

    BAD_REQUEST(400, "bad_request"), //错误请求

    USER_NOT_EXIST(400, "user not exist"), //用户不存在

    USER_AlREADY_EXIST(400, "user already exist"), //用户已存在

    USER_AlREADY_Login(400, "user already login"), //用户已登入

    USER_NOT_Login(400, "user not login"), //用户已登入

    CLIENT_NOT_FOUND(500, "client not found or client unreachable"), //无法找到网关连接到的客户端

    GATEWAY_NOT_FOUND(500, "gateway not found or gateway unreachable"), //无法找到任何网关或网关不可达

    SERVER_NOT_FOUND(500, "server not found"); //无法找到任何服务器

    private final long code; //http码

    private final String message; //错误信息

    private FailEnums(long code, String message) {
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
