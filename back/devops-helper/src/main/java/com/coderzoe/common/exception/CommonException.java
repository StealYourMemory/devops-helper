package com.coderzoe.common.exception;

/**
 * 基于RuntimeException的自定义异常
 * 主要是为了方便以后自定义功能的扩充
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:37
 */
public class CommonException extends RuntimeException {
    public CommonException() {
    }

    public CommonException(String message) {
        super(message);
    }

    public CommonException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommonException(Throwable cause) {
        super(cause);
    }

    public CommonException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
