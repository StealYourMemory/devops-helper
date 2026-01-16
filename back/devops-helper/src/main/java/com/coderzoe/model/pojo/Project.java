package com.coderzoe.model.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 项目信息
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:05
 */
@Data
@NoArgsConstructor
public class Project {
    private String name;
    private String gitlabUrl;
    private String jenkinsUrl;
    private String k8sDeployment;
    private String k8sDeploymentImage;
    /**
     * 是否禁用项目 true 禁用;false 不禁用 默认false
     */
    private boolean disabled;


    /**
     * 重写equals和hash
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Project project = (Project) o;
        return Objects.equals(name, project.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
