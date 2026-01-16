package com.coderzoe.component.gitlab.service;

import com.coderzoe.common.Result;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.component.gitlab.config.GitLabConfig;
import com.coderzoe.component.gitlab.model.BranchesResponse;
import com.coderzoe.config.ProjectConfig;
import com.coderzoe.model.pojo.LoginCookie;
import com.coderzoe.model.pojo.Project;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * gitlab业务层
 *
 * @author yinhuasheng
 * @date 2024/8/19 14:41
 */
@Service
@Setter(onMethod_ = @Autowired)
public class GitLabBranchService {
    private ProjectConfig projectConfig;
    private GitLabConfig gitLabConfig;
    private GitLabLoginService gitLabLoginService;
    private RestTemplate restTemplate;

    public Result<BranchesResponse> getBranches(String projectName) {
        Project project = projectConfig.get(projectName);
        String url = gitLabConfig.getGetBranchesUrl(project.getGitlabUrl());

        Result<BranchesResponse> result = doGetBranches(url);
        //如果不成功往往代表当前cookie失效，需要重新登录，重新登录后再获取分支
        if (!result.isSuccess()) {
            Result<LoginCookie> login = gitLabLoginService.login();
            if (!login.isSuccess()) {
                throw new CommonException("gitlab登录失败，失败原因:" + login.getMsg());
            }
            result = doGetBranches(url);
        }
        return result;
    }

    public Result<BranchesResponse> doGetBranches(String url) {
        //拿到cookie然后请求数据
        try {
            HttpEntity<String> entity = new HttpEntity<>(getGitLabHeaders());
            ResponseEntity<BranchesResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, BranchesResponse.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return Result.success(response.getBody());
            } else {
                return Result.fail("HTTP响应码:" + response.getStatusCode() + "可能是由于gitlab cookie 失效");
            }
        } catch (Exception e) {
            return Result.fail("请求失败可能是由于gitlab cookie 失效");
        }
    }


    private HttpHeaders getGitLabHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Cookie", gitLabLoginService.getCookie().toString());
        return headers;
    }
}
