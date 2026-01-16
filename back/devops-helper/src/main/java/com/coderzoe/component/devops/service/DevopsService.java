package com.coderzoe.component.devops.service;

import com.coderzoe.common.Result;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.component.devops.config.DevopsConfig;
import com.coderzoe.component.devops.model.*;
import com.coderzoe.component.devops.service.check.DevopsBugCheck;
import com.coderzoe.component.devops.service.check.DevopsRequestCheck;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author yinhuasheng
 * @date 2024/8/19 19:27
 */
@Service
@Setter(onMethod = @__(@Autowired))
public class DevopsService {
    private DevopsConfig devopsConfig;
    private DevopsLoginService devopsLoginService;
    private RestTemplate restTemplate;
    private List<DevopsRequestCheck> requestCheckList;
    private List<DevopsBugCheck> bugCheckList;
    public static final String OK = "ok";


    /**
     * 查询并检查devops需求列表
     *
     * @param sprints 迭代号
     * @return 返回需求集合
     */
    public Result<List<DevopsWorkIssueWrapper>> getAndCheckDevopsRequestList(List<String> sprints) {
        Result<List<DevopsWorkIssueResponse>> result = this.getRequestListBySprints(sprints);
        if (!result.isSuccess()) {
            return Result.fail(result.getMsg());
        }
        //这里有个小问题，如果子需求无委派人，则将子需求的委派人设为父需求的委派人
        fillAssignName(result.getData(), "");
        List<DevopsWorkIssueWrapper> wrapperList = result.getData()
                .stream()
                .map(DevopsWorkIssueWrapper::new)
                .map(p -> {
                    for (DevopsRequestCheck check : requestCheckList) {
                        p = check.check(p);
                    }
                    return p;
                }).collect(Collectors.toList());
        return Result.success(wrapperList);
    }

    /**
     * 查询并检查devops bug集合
     *
     * @param sprints 迭代号
     * @return 返回bug集合
     */
    public Result<List<DevopsWorkIssueWrapper>> getAndCheckDevopsBugList(List<String> sprints) {
        Result<List<DevopsWorkIssueResponse>> result = this.getBugListBySprints(sprints);
        if (!result.isSuccess()) {
            return Result.fail(result.getMsg());
        }
        List<DevopsWorkIssueWrapper> wrapperList = result.getData()
                .stream()
                .map(DevopsWorkIssueWrapper::new)
                .map(p -> {
                    for (DevopsBugCheck check : bugCheckList) {
                        p = check.check(p);
                    }
                    return p;
                }).collect(Collectors.toList());
        return Result.success(wrapperList);
    }

    /**
     * 查询公司成员信息，主要查询devops下这些成员的邮件用于邮件发送
     */
    public Result<List<DevopsCompanyMember>> getCompanyMember() {
        Result<List<DevopsCompanyMember>> result = doGetCompanyMember();
        if (!result.isSuccess()) {
            Result<DevopsToken> tokenResult = devopsLoginService.login();
            if (!tokenResult.isSuccess()) {
                throw new CommonException("devops登录失败，失败原因:" + tokenResult.getMsg());
            }
            result = doGetCompanyMember();
        }
        return result;
    }


    /**
     * 获取devops迭代集合
     *
     * @return 返回迭代的集合
     */
    public Result<List<DevopsSprintOptionResponse>> getDevopsSprintOptions() {
        Result<List<DevopsSprintOptionResponse>> result = doGetDevopsSprintOptions();
        if (!result.isSuccess()) {
            Result<DevopsToken> tokenResult = devopsLoginService.login();
            if (!tokenResult.isSuccess()) {
                throw new CommonException("devops登录失败，失败原因:" + tokenResult.getMsg());
            }
            result = doGetDevopsSprintOptions();
        }
        return result;
    }

    /**
     * 查询devops需求列表
     *
     * @param sprints 迭代周期
     * @return 返回需求列表
     */
    public Result<List<DevopsWorkIssueResponse>> getRequestListBySprints(List<String> sprints) {
        Result<List<DevopsWorkIssueResponse>> result = doGetRequestListBySprints(sprints);
        if (!result.isSuccess()) {
            Result<DevopsToken> tokenResult = devopsLoginService.login();
            if (!tokenResult.isSuccess()) {
                throw new CommonException("devops登录失败，失败原因:" + tokenResult.getMsg());
            }
            result = doGetRequestListBySprints(sprints);
        }
        return result;
    }

    /**
     * 查询devops bug列表
     *
     * @param sprints 迭代周期
     * @return 返回bug列表
     */
    public Result<List<DevopsWorkIssueResponse>> getBugListBySprints(List<String> sprints) {
        Result<List<DevopsWorkIssueResponse>> result = doGetBugListBySprints(sprints);
        if (!result.isSuccess()) {
            Result<DevopsToken> tokenResult = devopsLoginService.login();
            if (!tokenResult.isSuccess()) {
                throw new CommonException("devops登录失败，失败原因:" + tokenResult.getMsg());
            }
            result = doGetBugListBySprints(sprints);
        }
        return result;
    }

    private Result<List<DevopsCompanyMember>> doGetCompanyMember() {
        try {
            ResponseEntity<DevopsResult<List<DevopsCompanyMember>>> responseEntity = restTemplate.exchange(devopsConfig.getCompanyMemberUr(),
                    HttpMethod.GET,
                    new HttpEntity<>(getHttpHeaders()),
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (responseEntity.getStatusCode() == HttpStatus.OK && OK.equalsIgnoreCase(Objects.requireNonNull(responseEntity.getBody()).getMessage())) {
                return Result.success(Objects.requireNonNull(responseEntity.getBody()).getData());
            } else {
                return Result.fail("HTTP响应码:", responseEntity.getStatusCode().toString(), "可能是由于devops登录失效");
            }
        } catch (Exception e) {
            return Result.fail("请求失败可能是由于devops登录失效");
        }
    }

    private Result<List<DevopsSprintOptionResponse>> doGetDevopsSprintOptions() {
        try {
            ResponseEntity<DevopsResult<List<DevopsSprintOptionResponse>>> responseEntity = restTemplate.exchange(devopsConfig.getSprintOptionsUrl(),
                    HttpMethod.GET,
                    new HttpEntity<>(getHttpHeaders()),
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (responseEntity.getStatusCode() == HttpStatus.OK && OK.equalsIgnoreCase(Objects.requireNonNull(responseEntity.getBody()).getMessage())) {
                return Result.success(Objects.requireNonNull(responseEntity.getBody()).getData());
            } else {
                return Result.fail("HTTP响应码:", responseEntity.getStatusCode().toString(), "可能是由于devops登录失效");
            }
        } catch (Exception e) {
            return Result.fail("请求失败可能是由于devops登录失效");
        }
    }

    private Result<List<DevopsWorkIssueResponse>> doGetRequestListBySprints(List<String> sprints) {
        try {
            ResponseEntity<DevopsResult<List<DevopsWorkIssueResponse>>> responseEntity = restTemplate.exchange(devopsConfig.getWorkIssueListFilterBySprintsUrl(sprints),
                    HttpMethod.GET,
                    new HttpEntity<>(getHttpHeaders()),
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (responseEntity.getStatusCode() == HttpStatus.OK && OK.equals(Objects.requireNonNull(responseEntity.getBody()).getMessage())) {
                return Result.success(Objects.requireNonNull(responseEntity.getBody()).getData());
            } else {
                return Result.fail("HTTP响应码:", responseEntity.getStatusCode().toString(), "可能是由于devops登录失效");
            }
        } catch (Exception e) {
            return Result.fail("请求失败可能是由于devops登录失效");
        }
    }

    private Result<List<DevopsWorkIssueResponse>> doGetBugListBySprints(List<String> sprints) {
        try {
            ResponseEntity<DevopsResult<List<DevopsWorkIssueResponse>>> responseEntity = restTemplate.exchange(devopsConfig.getBugListFilterBySprintsUrl(sprints),
                    HttpMethod.GET,
                    new HttpEntity<>(getHttpHeaders()),
                    new ParameterizedTypeReference<>() {
                    }
            );
            if (responseEntity.getStatusCode() == HttpStatus.OK
                    && OK.equals(Objects.requireNonNull(responseEntity.getBody()).getMessage())) {
                return Result.success(Objects.requireNonNull(responseEntity.getBody()).getData());
            } else {
                return Result.fail("HTTP响应码:", responseEntity.getStatusCode().toString(), "可能是由于devops登录失效");
            }
        } catch (Exception e) {
            return Result.fail("请求失败可能是由于devops登录失效");
        }
    }

    public HttpHeaders getHttpHeaders() {
        DevopsToken devopsToken = devopsLoginService.getDevopsToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Cookie", "heytoken=; devopscloudtoken=null; loginState=true");
        httpHeaders.add("X-Auth-Token", devopsToken.getUserToken());
        httpHeaders.add("User_id", devopsToken.getUserId());
        return httpHeaders;
    }

    /**
     * 填充责任人
     * 很多时候我们在创建需求的时候，子需求可能不设责任人。
     * 比如特性指给了张三，张三自己拆了5个用户故事在这个特性下，但这5个用户故事都没将责任人改为自己
     * 针对这种情况，我们将子需求没设责任人的情况下将责任人设为父需求的责任人
     * 这主要是为了页面上点击邮件发送，对于子需求有问题，且没责任人的话可能不好找发送邮件的目的人
     */
    private static void fillAssignName(List<DevopsWorkIssueResponse> list, String parent) {
        for (DevopsWorkIssueResponse item : list) {
            if (!StringUtils.hasLength(item.getAssigneeName()) || "--".equals(item.getAssigneeName())) {
                item.setAssigneeName(parent);
            }
            if (!CollectionUtils.isEmpty(item.getChildren())) {
                fillAssignName(item.getChildren(), item.getAssigneeName());
            }
        }
    }
}
