package com.coderzoe.component.devops.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/19 18:04
 */
@Data
public class DevopsWorkIssueResponse {
    private String id;
    private String key;
    private String projectId;
    private String subject;
    private String workflowId;
    private String stateName;
    private String description;
    private String assigneeId;
    private String sprintId;
    private List<Version> versionInfoList;
    private String parentSubject;
    private String parentIssueLine;
    private String parentKey;
    private String authorName;
    private String assigneeName;
    private String priorityText;
    private String severityText;
    private String sprintName;
    private String categoryName;
    @JsonProperty("is_parent")
    private Boolean parent;
    private List<DevopsWorkIssueResponse> children;
    private String resolvedVersionName;
    private String projectIssueTypeName;
    private String projectName;
    @JsonProperty("LaneId")
    private String laneId;


    @Data
    public static class Version {
        private String id;
        private String name;
    }
}
