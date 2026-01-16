package com.coderzoe.component.devops.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author yinhuasheng
 * @date 2024/8/19 18:03
 */
@Data
public class DevopsResult<T> {
    /**
     * 响应值
     */
    @JsonProperty("code")
    private String code;
    /**
     * 消息
     */
    @JsonProperty("msg")
    private String message;

    /**
     * 响应实体
     */
    @JsonProperty("data")
    private T data;

    @JsonProperty("total")
    private Integer total;
}
