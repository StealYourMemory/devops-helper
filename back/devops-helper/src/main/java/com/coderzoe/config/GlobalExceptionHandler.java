package com.coderzoe.config;

import com.coderzoe.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常拦截器
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:01
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<?> handleDeviceExecutionException(Exception e) {
        log.error("异常:", e);
        return Result.fail(e.getMessage());
    }
}
