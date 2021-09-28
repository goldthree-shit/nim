package com.justafewmistakes.nim.common.api;

import com.justafewmistakes.nim.common.excpetion.FailEnums;
import com.justafewmistakes.nim.common.excpetion.IErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回对象
 * Created by macro on 2019/4/19.
 */
@Data
public class CommonResult<T> implements Serializable {
    private long code;
    private String message;
    private T data;

    protected CommonResult() {
    }

    protected CommonResult(long code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(ResultEnums.SUCCESS.getCode(), ResultEnums.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     * @param  message 提示信息
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<T>(ResultEnums.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败返回结果
     * @param errorCode 错误码
     */
    public static <T> CommonResult<T> failed(IErrorCode errorCode) {
        return new CommonResult<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回结果
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static <T> CommonResult<T> failed(IErrorCode errorCode,String message) {
        return new CommonResult<T>(errorCode.getCode(), message, null);
    }

    /**
     * 失败返回结果
     * @param errorCode 错误码
     * @param message 错误信息
     */
    public static <T> CommonResult<T> failed(long errorCode,String message) {
        return new CommonResult<T>(errorCode, message, null);
    }

    /**
     * 失败返回结果
     * @param message 提示信息
     */
    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>(FailEnums.FAIL.getCode(), message, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> failed() {
        return failed(FailEnums.FAIL);
    }

}
