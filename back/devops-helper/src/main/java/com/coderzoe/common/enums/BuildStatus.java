package com.coderzoe.common.enums;

import lombok.Getter;

/**
 * @author yinhuasheng
 * @date 2024/8/19 14:00
 */
@Getter
public enum BuildStatus {
    /**
     * 构建状态
     */
    Success("Success"),
    Failed("Failed"),
    Pending("Pending"),
    Aborted("Aborted");
    private final String value;

    BuildStatus(String value) {
        this.value = value;
    }
}
