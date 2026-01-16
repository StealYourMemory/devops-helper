package com.coderzoe.common.enums;

import lombok.Getter;

/**
 * @author yinhuasheng
 * @date 2024/8/19 14:00
 */
@Getter
public enum BuildAndDeployTypeEnum {
    /**
     * 枚举类
     */
    BUILD_AND_DEPLOY("打包和部署"),
    BUILD_ONLY("仅打包"),
    DEPLOY_ONLY("仅部署"),
    BUILD_AND_TRANSFER("打包和转测"),
    TRANSFER_AND_DEPLOY("转测和部署");
    private final String description;

    BuildAndDeployTypeEnum(String description) {
        this.description = description;
    }
}
