package com.coderzoe.component.k8s.config;

import com.coderzoe.component.k8s.model.K8sEnvAndServerInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author yinhuasheng
 * @date 2024/8/19 15:36
 */
@ConfigurationProperties(prefix = "k8s.init")
@Configuration
@Setter
@Getter
public class K8sInitEnvAndServerConfig {
    private Map<String, K8sEnvAndServerInfo> initEnvAndServer;
}
