package com.coderzoe.component.seafile.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/20 11:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteDirRequest {
    @JsonProperty("parent_dir")
    private String parentDir;
    @JsonProperty("dirents")
    private List<String> dirList;
    @JsonProperty("repo_id")
    private String repoId;
}
