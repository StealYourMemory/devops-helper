package com.coderzoe.config;

import com.coderzoe.common.exception.CommonException;
import com.coderzoe.model.pojo.Project;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * 项目的配置
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:04
 */
@ConfigurationProperties(prefix = "devops-helper.project")
@Configuration
@Setter
@Getter
public class ProjectConfig {
    private Set<Project> projects;

    public Project get(String projectName) {
        return this.projects
                .stream()
                .filter(p -> p.getName().equals(projectName))
                .findFirst()
                .orElseThrow(() -> new CommonException("未找到名为" + projectName + "的项目"));
    }
}
