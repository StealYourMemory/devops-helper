package com.coderzoe.common.enums;

import lombok.Getter;

/**
 * 环境类型
 *
 * @author yinhuasheng
 * @date 2024/8/19 14:01
 */
@Getter
public enum EnvironmentTypeEnum {
    /**
     * 环境额外配置
     */
    NEED_DOCKER_PULL("需要先使用docker pull拉下来镜像，如城轨环境"),
    REBIRTH_V2("rebirth2.0"),
    REBIRTH_V1("rebirth1.0");
    private final String description;

    EnvironmentTypeEnum(String description) {
        this.description = description;
    }
}
