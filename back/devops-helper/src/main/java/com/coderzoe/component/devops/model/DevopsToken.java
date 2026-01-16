package com.coderzoe.component.devops.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yinhuasheng
 * @date 2024/8/19 18:04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevopsToken {
    private String userName;
    private String userToken;
    private String userId;
}
