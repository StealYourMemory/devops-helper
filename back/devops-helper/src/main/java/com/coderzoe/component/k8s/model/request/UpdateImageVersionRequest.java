package com.coderzoe.component.k8s.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateImageVersionRequest {
    private String environment;
    private String projectName;
    private String tag;
    /**
     * 2024-08-19 侵入到业务代码的详细打包进度
     */
    private String requestId;
}
