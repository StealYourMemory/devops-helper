package com.coderzoe.component.jenkins.model;

import com.coderzoe.common.enums.BuildStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * jenkins打包记录
 *
 * @author yinhuasheng
 * @date 2024/8/19 15:02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuildHistory {
    private int id;
    private String projectName;
    private String branch;
    private String tag;
    private BuildStatus status;
    private long tookTime;
    private long remainingTime;
    private long date;
}
