package com.coderzoe.component.devops.service.check;

import com.coderzoe.common.Constants;
import com.coderzoe.component.devops.model.DevopsWorkIssueWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/19 19:30
 */
@Service
public class LevelCheck implements DevopsRequestCheck {
    public static final String LEVEL1 = "史诗";
    public static final String LEVEL2 = "特性";
    public static final String LEVEL3 = "用户故事";
    public static final String LEVEL4 = "任务";

    @Override
    public DevopsWorkIssueWrapper check(DevopsWorkIssueWrapper response) {
        if (LEVEL1.equals(response.getProjectIssueTypeName())) {
            handleLevel1(response);
        }
        if (LEVEL2.equals(response.getProjectIssueTypeName())) {
            handleLevel2(response);
        }
        if (LEVEL3.equals(response.getProjectIssueTypeName())) {
            handleLevel3(response);
        }
        return response;
    }


    /**
     * 处理史诗
     */
    private void handleLevel1(DevopsWorkIssueWrapper response) {
        List<DevopsWorkIssueWrapper> children = response.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            response.setSuccess(false);
            response.getSuggest().add("史诗底下没有特性");
        } else {
            for (DevopsWorkIssueWrapper child : children) {
                if (!LEVEL2.equals(child.getProjectIssueTypeName())) {
                    child.setSuccess(false);
                    child.getSuggest().add("史诗底下只能挂特性，不能挂其他");
                }
            }
            children.stream().filter(p -> LEVEL2.equals(p.getProjectIssueTypeName())).forEach(this::handleLevel2);
        }
    }

    /**
     * 处理特性
     */
    private void handleLevel2(DevopsWorkIssueWrapper response) {
        List<DevopsWorkIssueWrapper> children = response.getChildren();
        if (CollectionUtils.isEmpty(children)) {
            response.setSuccess(false);
            response.getSuggest().add("特性底下没有用户故事");
        } else {
            List<DevopsWorkIssueWrapper> level3Children = children.stream().filter(p -> LEVEL3.equals(p.getProjectIssueTypeName())).toList();
            if (level3Children.size() < Constants.DEVOPS_LEVEL2_CHILDREN_MIN_SIZE) {
                response.setSuccess(false);
                response.getSuggest().add("建议特性底下至少挂5个用户故事");
            }
            for (DevopsWorkIssueWrapper child : children) {
                if (!LEVEL3.equals(child.getProjectIssueTypeName())) {
                    child.setSuccess(false);
                    child.getSuggest().add("特性底下只能挂用户故事，不能挂其他");
                }
            }
            children.stream().filter(p -> LEVEL3.equals(p.getProjectIssueTypeName())).forEach(this::handleLevel3);
        }
    }

    private void handleLevel3(DevopsWorkIssueWrapper response) {
        List<DevopsWorkIssueWrapper> children = response.getChildren();
        if (!CollectionUtils.isEmpty(children)) {
            for (DevopsWorkIssueWrapper child : children) {
                if (!LEVEL4.equals(child.getProjectIssueTypeName())) {
                    child.setSuccess(false);
                    child.getSuggest().add("用户故事底下只能挂任务，不能挂其他");
                }
            }
        }
    }
}
