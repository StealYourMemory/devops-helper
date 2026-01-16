package com.coderzoe.component.k8s.model.request;

import com.coderzoe.component.k8s.model.database.K8sProxyRecordDO;
import com.coderzoe.model.pojo.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProxyRecordQueryRequest extends Page<K8sProxyRecordDO> {
    private String envName;
    private String proxyHost;
    private String srcHost;
    private Long startTime;
    private Long endTime;
}
