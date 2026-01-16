package com.coderzoe.component.gitlab.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author yinhuasheng
 * @date 2024/8/19 14:11
 */
@Data
@Builder
public class AuthKey {
    private String authKey;
    private String cookie;
}
