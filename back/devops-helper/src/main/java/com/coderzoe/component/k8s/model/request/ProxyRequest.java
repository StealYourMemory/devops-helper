package com.coderzoe.component.k8s.model.request;

import lombok.Data;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:02
 */
@Data
public class ProxyRequest {
    private String envName;
    private String srcHost;
    private int srcPort;
    private String proxyHost;
    private String description;
    private int proxyPort;
}
