package com.coderzoe.component.k8s.model.request;

import lombok.Data;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:01
 */
@Data
public class DeleteServerRequest {
    private String envName;
    private String ip;
    private int port;
}
