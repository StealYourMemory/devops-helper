package com.coderzoe.component.jenkins.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author yinhuasheng
 * @date 2024/8/19 15:27
 */
@Data
public class JenkinsCrumbResponse {
    @JsonProperty("_class")
    private String clazz;
    @JsonProperty("crumb")
    private String crumb;
    @JsonProperty("crumbRequestField")
    private String requestField;
}
