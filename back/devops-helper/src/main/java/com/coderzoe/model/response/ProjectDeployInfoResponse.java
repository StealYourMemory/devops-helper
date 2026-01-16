package com.coderzoe.model.response;

import com.coderzoe.common.enums.BuildStatus;
import com.coderzoe.component.jenkins.model.BuildHistory;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/9/13 16:56
 */
@Data
@NoArgsConstructor
public class ProjectDeployInfoResponse {
    private int id;
    private String envName;
    private String projectName;
    private String branch;
    private String tag;
    private BuildStatus status;
    private long tookTime;
    private long remainingTime;
    private long date;

    public ProjectDeployInfoResponse(BuildHistory buildHistory,String envName) {
        this.id = buildHistory.getId();
        this.envName = envName;
        this.projectName = buildHistory.getProjectName();
        this.branch = buildHistory.getBranch();
        this.tag = buildHistory.getTag();
        this.status = buildHistory.getStatus();
        this.tookTime = buildHistory.getTookTime();
        this.remainingTime = buildHistory.getRemainingTime();
        this.date = buildHistory.getDate();
    }
}
