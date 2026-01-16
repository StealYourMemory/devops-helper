package com.coderzoe.component.devops.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 18:02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevopsLoginRequest {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String password;
    /**
     * 登录类型
     */
    private String type = "external";
}
