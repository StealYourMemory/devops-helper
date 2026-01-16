package com.coderzoe.common.log;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author yinhuasheng
 * @date 2024/8/19 11:43
 */
@Setter
@Getter
@NoArgsConstructor
public class LogInfo {
    /**
     * 标题 当前记录日志的简述 如用户登录
     */
    private String title;

    /**
     * 操作方法 正常写法为类名+方法签名
     */
    private String method;

    /**
     * 操作详情 用于描述当前操作的详细信息
     */
    private String detail;

    /**
     * 操作结果 true为操作成功 false为操作失败
     */
    private Boolean success;

    /**
     * 结果详情 用于描述操作结果的详细信息，如果操作结果成功 当前字段有意义
     */
    private String result;
    /**
     * 失败原因 描述失败的失败原因，如果操作失败 当前字段有意义
     */
    private String failReason;

    /**
     * 日志级别
     */
    private LogLevel logLevel;
    /**
     * 日志记录的时间
     */
    private Long time;

    public static LogInfo success(String title, String method, String detail, String result, LogLevel logLevel) {
        LogInfo log = new LogInfo();
        log.title = title;
        log.method = method;
        log.detail = detail;
        log.result = result;
        log.logLevel = logLevel;
        log.success = true;
        log.failReason = null;
        log.time = System.currentTimeMillis();
        return log;
    }

    public static LogInfo error(String title, String method, String detail, String failReason, LogLevel logLevel) {
        LogInfo log = new LogInfo();
        log.title = title;
        log.method = method;
        log.detail = detail;
        log.logLevel = logLevel;
        log.result = null;
        log.success = false;
        log.failReason = failReason;
        log.time = System.currentTimeMillis();
        return log;
    }

    @Override
    public String toString() {
        if (this.success) {
            return "{" +
                    "标题='" + title + '\'' +
                    ", 方法名='" + method + '\'' +
                    ", 方法详情='" + detail + '\'' +
                    ", 是否成功=成功 " +
                    ", 返回结果='" + result +
                    '}';
        } else {
            return "{" +
                    "标题='" + title + '\'' +
                    ", 方法名='" + method + '\'' +
                    ", 方法详情='" + detail + '\'' +
                    ", 是否成功=失败 " +
                    ", 失败原因='" + failReason +
                    '}';
        }
    }
}
