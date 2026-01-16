package com.coderzoe.model.response;

import com.coderzoe.common.Result;
import com.coderzoe.component.jenkins.model.BuildHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildAndDeployProgressResponse {
    private String requestId;
    private Result<BuildHistory> buildProgress;
    private List<String> deployProgress;

    public BuildAndDeployProgressResponse(String requestId) {
        this.requestId = requestId;
    }
}
