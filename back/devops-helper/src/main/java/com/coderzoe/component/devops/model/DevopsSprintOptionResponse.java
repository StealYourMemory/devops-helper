package com.coderzoe.component.devops.model;

import lombok.Data;

/**
 * @author yinhuasheng
 * @date 2024/8/19 18:03
 */
@Data
public class DevopsSprintOptionResponse {
    private String id;
    private String projectId;
    private String name;
    private String goal;
    private String state;
}
