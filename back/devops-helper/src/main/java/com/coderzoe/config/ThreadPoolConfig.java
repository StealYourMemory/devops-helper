package com.coderzoe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 线程池配置
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:18
 */
@ConfigurationProperties(prefix = "devops-helper.thread.pool")
@Configuration
@Getter
@Setter
public class ThreadPoolConfig {
    private int corePoolSize = 100;
    private int maximumPoolSize = 200;
    private long keepAliveTime = 300;
}
