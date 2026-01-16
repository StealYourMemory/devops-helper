package com.coderzoe.component.jenkins.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 向jenkins发build请求的参数
 *
 * @author yinhuasheng
 * @date 2024/8/19 15:29
 */
@Data
@NoArgsConstructor
public class JenkinsBuildRequest {
    private List<KeyValue> parameter;
    private String statusCode;
    private String redirectTo;
    @JsonProperty("Jenkins-Crumb")
    private String crumb;

    public JenkinsBuildRequest(String branch, String dockerTag, String crumb) {
        this.setParameter(branch, dockerTag);
        this.crumb = crumb;
        this.statusCode = "303";
        this.redirectTo = ".";
    }

    public void setParameter(String branch, String dockerTag) {
        this.parameter = new ArrayList<>(2);
        parameter.add(new KeyValue("BRANCH", branch));
        parameter.add(new KeyValue("DOCKER_TAG", dockerTag));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyValue {
        public String name;
        public String value;
    }
}
