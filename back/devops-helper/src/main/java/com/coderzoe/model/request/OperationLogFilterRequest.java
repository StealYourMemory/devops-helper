package com.coderzoe.model.request;

import com.coderzoe.model.database.OperationLogDO;
import com.coderzoe.model.pojo.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:58
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OperationLogFilterRequest extends Page<OperationLogDO> {
    private String user;
    private String operation;
    private Boolean success;
    private Long startTime;
    private Long endTime;
}
