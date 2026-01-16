package com.coderzoe.component.seafile.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author yinhuasheng
 * @date 2024/8/20 10:40
 */
@Data
public class DirectoryInfo {
    private String type;
    private String id;
    private String name;
    @JsonProperty("mtime")
    private Long time;
    private String permission;
    @JsonProperty("parent_dir")
    private String parentDirectory;
    private boolean starred;
    /**
     * 以下属性只有在type是file时才有值
     */
    private Long size;
    @JsonProperty("modifier_email")
    private String modifierEmail;
    @JsonProperty("modifier_name")
    private String modifierName;
    @JsonProperty("modifier_contact_email")
    private String modifierContactEmail;
}
