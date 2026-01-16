package com.coderzoe.component.jenkins.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * jenkins打包请求
 *
 * @author yinhuasheng
 * @date 2024/8/19 15:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildRequest {
    private String projectName;
    private String branch;
    private String tag;
}
