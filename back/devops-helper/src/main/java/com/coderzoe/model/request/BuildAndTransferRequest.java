package com.coderzoe.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:57
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildAndTransferRequest {
    /**
     * 项目名
     */
    @NotEmpty
    private String projectName;
    /**
     * 分支
     */
    @NotEmpty
    private String branch;
    /**
     * 打包的tag
     */
    @NotEmpty
    private String tag;
    /**
     * 更新说明
     */
    private String updateInfo;
}
