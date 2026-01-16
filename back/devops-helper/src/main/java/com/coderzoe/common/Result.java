package com.coderzoe.common;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

/**
 * 通用返回
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:02
 */
@Getter
@ToString
public class Result<T> {
    private boolean success;
    private String msg;
    private T data;

    private Result() {
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.success = true;
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(String... msg) {
        Result<T> result = new Result<>();
        result.success = false;
        result.msg = Arrays.toString(msg);
        return result;
    }
}
