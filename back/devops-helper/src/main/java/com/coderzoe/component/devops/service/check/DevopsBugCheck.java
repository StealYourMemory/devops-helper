package com.coderzoe.component.devops.service.check;

import com.coderzoe.component.devops.model.DevopsWorkIssueWrapper;

/**
 * @author yinhuasheng
 * @date 2024/8/19 19:29
 */
public interface DevopsBugCheck {
    /**
     * 校验结果是否正常
     *
     * @param response 元数据
     * @return 校验结果
     */
    DevopsWorkIssueWrapper check(DevopsWorkIssueWrapper response);
}
