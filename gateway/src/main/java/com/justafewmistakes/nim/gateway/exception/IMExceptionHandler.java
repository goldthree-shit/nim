package com.justafewmistakes.nim.gateway.exception;

import com.justafewmistakes.nim.common.api.CommonResult;
import com.justafewmistakes.nim.common.excpetion.IMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@RestControllerAdvice
public class IMExceptionHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(IMExceptionHandler.class);

    @ExceptionHandler(IMException.class)
    public CommonResult handleIMException(IMException exception) {
        LOGGER.error("错误");
        return CommonResult.failed(exception.getCode(), exception.getMessage());
    }
}
