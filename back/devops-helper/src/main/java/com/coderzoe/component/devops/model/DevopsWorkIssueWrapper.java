package com.coderzoe.component.devops.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yinhuasheng
 * @date 2024/8/19 18:04
 */
@Data
public class DevopsWorkIssueWrapper {
    private boolean success;
    private List<String> suggest;

    private String id;
    private String key;
    private String projectId;
    private String subject;
    private String workflowId;
    private String stateName;
    private String description;
    private String assigneeId;
    private String sprintId;
    private List<DevopsWorkIssueResponse.Version> versionInfoList;
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
    private List<DevopsWorkIssueWrapper> children;
    private String resolvedVersionName;
    private String projectIssueTypeName;
    private String projectName;
    @JsonProperty("LaneId")
    private String laneId;

    public DevopsWorkIssueWrapper() {
        this.success = true;
        this.suggest = new ArrayList<>();
    }

    public DevopsWorkIssueWrapper(DevopsWorkIssueResponse response) {
        this();
        this.id = response.getId();
        this.key = response.getKey();
        this.projectId = response.getProjectId();
        this.subject = response.getSubject();
        this.workflowId = response.getWorkflowId();
        this.stateName = response.getStateName();
        this.description = response.getDescription();
        this.assigneeId = response.getAssigneeId();
        this.sprintId = response.getSprintId();
        this.versionInfoList = response.getVersionInfoList();
        this.parentSubject = response.getParentSubject();
        this.parentIssueLine = response.getParentIssueLine();
        this.parentKey = response.getParentKey();
        this.authorName = response.getAuthorName();
        this.assigneeName = response.getAssigneeName();
        this.priorityText = response.getPriorityText();
        this.severityText = response.getSeverityText();
        this.sprintName = response.getSprintName();
        this.categoryName = response.getCategoryName();
        this.parent = response.getParent();
        if (response.getChildren() != null) {
            this.children = response.getChildren()
                    .stream()
                    .map(DevopsWorkIssueWrapper::new)
                    .collect(Collectors.toList());
        }
        this.resolvedVersionName = response.getResolvedVersionName();
        this.projectIssueTypeName = response.getProjectIssueTypeName();
        this.projectName = response.getProjectName();
        this.laneId = response.getLaneId();
    }
}
