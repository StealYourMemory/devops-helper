package com.coderzoe.component.gitlab.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/19 14:10
 */
@Data
public class BranchesResponse {
    @JsonProperty("Branches")
    private List<String> branches;
    @JsonProperty("Tags")
    private List<String> tags;
    @JsonProperty("Commits")
    private List<String> commits;
}
