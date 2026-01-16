package com.coderzoe.component.k8s.model;

import lombok.Data;

import java.time.Duration;

/**
 * @author yinhuasheng
 * @date 2024/8/19 16:07
 */
@Data
public class K8sServerConnectConfig {
    private Duration connectTimeout = Duration.ofSeconds(30);
    private Duration authTimeout = Duration.ofSeconds(30);
    private Duration readTimeout = Duration.ofSeconds(30);
    private Duration writeTimeout = Duration.ofSeconds(30);
}
