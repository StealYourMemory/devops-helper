package com.coderzoe.component.devops.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * devops成员信息
 *
 * @author yinhuasheng
 * @date 2024/8/21 09:30
 */
@Data
public class DevopsCompanyMember {
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("enter_time")
    private String enterTime;
    @JsonProperty("role_id")
    private String roleId;
    private String name;
    @JsonProperty("show_name")
    private String showName;
    private String email;
    private String phone;
    private String type;
    @JsonProperty("role_name")
    private String roleName;
}
