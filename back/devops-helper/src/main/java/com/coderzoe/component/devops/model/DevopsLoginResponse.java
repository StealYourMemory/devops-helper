package com.coderzoe.component.devops.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/19 18:02
 */
@Data
public class DevopsLoginResponse {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("fullname")
    private String fullName;

    @JsonProperty("ext_unique_field")
    private String extUniqueField;

    @JsonProperty("token")
    private String token;

    @JsonProperty("type")
    private String type;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("instance_info")
    private List<Instance> instanceList;

    @Data
    public static class Instance {
        @JsonProperty("instance_id")
        private String instanceId;

        @JsonProperty("company_name")
        private String companyName;

        @JsonProperty("role_name")
        private String roleName;
    }
}
