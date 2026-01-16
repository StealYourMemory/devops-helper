package com.coderzoe.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:57
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildOnlyRequest {
    /**
     * 项目名
     */
    @NotEmpty
    private String projectName;

    /**
     * 打包的tag
     */
    @NotEmpty
    private String tag;
    /**
     * 分支
     */
    @NotEmpty
    private String branch;
}
