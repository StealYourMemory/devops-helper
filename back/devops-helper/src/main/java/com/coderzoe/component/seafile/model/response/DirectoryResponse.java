package com.coderzoe.component.seafile.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/20 10:40
 */
@Data
public class DirectoryResponse {
    @JsonProperty("user_perm")
    private String userPermission;
    @JsonProperty("dir_id")
    private String directoryId;
    @JsonProperty("dirent_list")
    private List<DirectoryInfo> directoryInfoList;
}
