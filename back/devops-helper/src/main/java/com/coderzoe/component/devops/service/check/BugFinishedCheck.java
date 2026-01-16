package com.coderzoe.component.devops.service.check;

import com.coderzoe.component.devops.model.DevopsWorkIssueWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author yinhuasheng
 * @date 2024/8/19 19:29
 */
@Service
public class BugFinishedCheck implements DevopsBugCheck {
    public static final String FINISHED = "已完成";

    @Override
    public DevopsWorkIssueWrapper check(DevopsWorkIssueWrapper response) {
        if (!FINISHED.equals(response.getStateName())) {
            response.setSuccess(false);
            response.getSuggest().add("当前bug状态不是已完成，如果完成请改为已完成，或者移到下一迭代版本");
        }
        //递归校验子元素
        if (!CollectionUtils.isEmpty(response.getChildren())) {
            response.getChildren().forEach(this::check);
        }
        return response;
    }
}
