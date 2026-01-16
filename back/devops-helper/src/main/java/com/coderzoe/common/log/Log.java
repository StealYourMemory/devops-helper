package com.coderzoe.common.log;

import java.lang.annotation.*;

/**
 * @author yinhuasheng
 * @date 2024/8/19 11:41
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 标题 对于当前方法的简述 如打包
     *
     * @return 返回标题
     */
    String title() default "";

    /**
     * 日志级别，允许使用者声明日志级别 默认是INFO级别
     *
     * @return 返回日志级别
     */
    LogLevel level() default LogLevel.INFO;
}
