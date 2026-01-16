package com.coderzoe.model.request;

import com.coderzoe.common.enums.BuildAndDeployTypeEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildAndDeployDTO {
    /**
     * 项目名
     */
    private String projectName;
    /**
     * docker tag
     */
    private String tag;
    /**
     * 环境
     */
    @NotNull
    private String environment;
    /**
     * 分支
     */
    @NotEmpty
    private String branch;

    /**
     * 用户名
     */
    private String user;

    /**
     * 请求id
     */
    private String requestId;

    private BuildAndDeployTypeEnum type;


    public boolean needBuild() {
        return this.getType() == BuildAndDeployTypeEnum.BUILD_AND_DEPLOY
                || this.getType() == BuildAndDeployTypeEnum.BUILD_ONLY
                || this.getType() == BuildAndDeployTypeEnum.BUILD_AND_TRANSFER;
    }

    public boolean needDeploy() {
        return this.getType() == BuildAndDeployTypeEnum.BUILD_AND_DEPLOY
                || this.getType() == BuildAndDeployTypeEnum.DEPLOY_ONLY
                || this.getType() == BuildAndDeployTypeEnum.TRANSFER_AND_DEPLOY;
    }
}
