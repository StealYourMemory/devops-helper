package com.coderzoe.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectBranchResponse {
    private List<String> branches;
    private List<String> tags;
    private List<String> commits;
}
