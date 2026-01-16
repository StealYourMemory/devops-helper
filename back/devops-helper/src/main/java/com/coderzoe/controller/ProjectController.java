package com.coderzoe.controller;

import com.coderzoe.common.Constants;
import com.coderzoe.common.Result;
import com.coderzoe.common.log.Log;
import com.coderzoe.common.util.DateUtil;
import com.coderzoe.component.gitlab.model.BranchesResponse;
import com.coderzoe.component.gitlab.service.GitLabBranchService;
import com.coderzoe.component.jenkins.model.BuildHistory;
import com.coderzoe.component.jenkins.service.JenkinsBuildService;
import com.coderzoe.component.k8s.model.K8sEnvAndServerInfo;
import com.coderzoe.component.k8s.service.K8sServerService;
import com.coderzoe.config.ProjectConfig;
import com.coderzoe.model.response.ProjectBranchResponse;
import com.coderzoe.model.response.ProjectInfoResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author yinhuasheng
 * @date 2024/8/19 19:32
 */
@RestController
@RequestMapping("/project/meta")
@Setter(onMethod_ = {@Autowired})
public class ProjectController {
    private ProjectConfig projectConfig;
    private GitLabBranchService gitLabBranchService;
    private JenkinsBuildService jenkinsBuildService;
    private K8sServerService k8sServerService;

    /**
     * 获取项目列表
     */
    @Log
    @GetMapping("/name")
    public Result<List<ProjectInfoResponse>> getProject() {
        List<ProjectInfoResponse> list = projectConfig.getProjects()
                .stream()
                .map(p -> new ProjectInfoResponse(p.getName(), p.isDisabled()))
                .collect(Collectors.toList());
        return Result.success(list);
    }

    /**
     * 获取某个项目下的分支列表
     *
     * @param projectName 项目名
     */
    @Log
    @GetMapping("/branch/{projectName}")
    public Result<ProjectBranchResponse> branch(@PathVariable String projectName) {
        Result<BranchesResponse> result = gitLabBranchService.getBranches(projectName);
        if (result.isSuccess()) {
            return Result.success(new ProjectBranchResponse(result.getData().getBranches(), result.getData().getTags(), result.getData().getCommits()));
        } else {
            return Result.fail(result.getMsg());
        }
    }

    /**
     * 获得环境
     *
     * @return 返回环境
     */
    @Log
    @GetMapping("/environment")
    public Result<List<K8sEnvAndServerInfo>> getEnvironment() {
        return Result.success(k8sServerService.getEnvAndServerList());
    }

    /**
     * 当想打包的时候输入项目名和分支 给出一个tag建议
     * tag建议的逻辑是：查询这个项目目前近30条所打的tag
     * 如果是release分支就按yyyyMMdd-vx形式给出建议
     * 如果是非release分支就按dh-yyyyMMdd-vx的形式
     */
    @Log
    @GetMapping("/tag/{projectName}/{branch}")
    public Result<String> getTag(@PathVariable String projectName, @PathVariable String branch) {
        Result<List<BuildHistory>> result = jenkinsBuildService.getBuildHistory(projectName, null, true);
        if (!result.isSuccess()) {
            return Result.fail(result.getMsg());
        }
        List<BuildHistory> resultData = result.getData();
        //获得当前日期
        String now = DateUtil.formatDate(new Date());
        //区分非release分支和release分支
        String tagPrefix = branch.equals(Constants.RELEASE_BRANCH) ? now + "-v" : Constants.DEVOPS_HELPER_TAG_PREFIX + now + "-v";
        return Result.success(getNextTag(resultData, tagPrefix));
    }

    /**
     * 查询jenkins打包历史 带筛选项
     */
    @Log
    @GetMapping("/build-history/{projectName}")
    public Result<List<BuildHistory>> getPackageHistory(@PathVariable String projectName, @RequestParam(required = false) String search) {
        return jenkinsBuildService.getBuildHistory(projectName, search, true);
    }

    /**
     * 查询打包详情
     *
     * @param projectName 项目名
     * @param buildId     打包id
     * @return 返回打包详情
     */
    @Log
    @GetMapping("/build-detail/{projectName}/{buildId}")
    public Result<String> getBuildDetail(@PathVariable String projectName, @PathVariable int buildId) {
        return jenkinsBuildService.getBuildDetail(projectName, buildId);
    }

    private String getNextTag(List<BuildHistory> buildHistoryList, String tagPrefix) {
        Optional<Integer> max = buildHistoryList.stream().map(BuildHistory::getTag)
                .filter(Objects::nonNull)
                .filter(p -> p.startsWith(tagPrefix))
                .map(p -> p.split(tagPrefix)[1]).map(p -> {
                    try {
                        return Integer.parseInt(p);
                    } catch (Exception e) {
                        return 0;
                    }
                }).max(Integer::compareTo);
        return max.map(integer -> tagPrefix + (integer + 1)).orElseGet(() -> tagPrefix + "1");
    }
}
