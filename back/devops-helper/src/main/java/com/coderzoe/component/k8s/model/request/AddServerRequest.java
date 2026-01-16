package com.coderzoe.component.k8s.model.request;

import com.coderzoe.component.k8s.model.K8sEnvAndServerInfo;
import lombok.Data;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:01
 */
@Data
public class AddServerRequest {
    private String envName;
    private K8sEnvAndServerInfo.K8sServerInfo serverInfo;
}
