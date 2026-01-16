package com.coderzoe.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:59
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectInfoResponse {
    private String projectName;
    private boolean disabled;
}
