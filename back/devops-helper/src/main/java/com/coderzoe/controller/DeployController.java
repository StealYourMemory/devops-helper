package com.coderzoe.controller;

import com.coderzoe.common.Result;
import com.coderzoe.common.ThreadPool;
import com.coderzoe.common.enums.BuildAndDeployTypeEnum;
import com.coderzoe.common.log.Log;
import com.coderzoe.common.util.Cache;
import com.coderzoe.common.util.UserGetter;
import com.coderzoe.model.request.BuildAndDeployDTO;
import com.coderzoe.model.request.BuildAndDeployRequest;
import com.coderzoe.model.request.BuildOnlyRequest;
import com.coderzoe.model.request.DeployOnlyRequest;
import com.coderzoe.model.response.BuildAndDeployProgressResponse;
import com.coderzoe.model.response.ProjectDeployInfoResponse;
import com.coderzoe.service.DeployService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author yinhuasheng
 * @date 2024/8/19 19:32
 */
@RestController
@RequestMapping("/common-deploy")
public class DeployController {
    private DeployService deployService;

    @PostMapping("/build-deploy")
    @Log(title = "打包和部署")
    public Result<String> buildAndDeploy(@RequestBody @Valid BuildAndDeployRequest request, HttpServletRequest servletRequest) {
        String requestId = UUID.randomUUID().toString();
        BuildAndDeployDTO req = BuildAndDeployDTO.builder()
                .projectName(request.getProjectName())
                .tag(request.getTag())
                .environment(request.getEnvironment())
                .branch(request.getBranch())
                .user(UserGetter.get(servletRequest))
                .requestId(requestId)
                .type(BuildAndDeployTypeEnum.BUILD_AND_DEPLOY)
                .build();
        ThreadPool.executorService().submit(() -> {
            deployService.buildAndDeploy(req);
        });
        //返回一个UUID用于事件追踪
        return Result.success(requestId);
    }

    @PostMapping("/build-only")
    @Log(title = "仅打包")
    public Result<String> buildOnly(@RequestBody @Valid BuildOnlyRequest request, HttpServletRequest servletRequest) {
        String requestId = UUID.randomUUID().toString();
        BuildAndDeployDTO req = BuildAndDeployDTO.builder()
                .projectName(request.getProjectName())
                .tag(request.getTag())
                .branch(request.getBranch())
                .user(UserGetter.get(servletRequest))
                .requestId(requestId)
                .type(BuildAndDeployTypeEnum.BUILD_ONLY)
                .build();
        ThreadPool.executorService().submit(() -> {
            deployService.buildAndDeploy(req);
        });
        //返回一个UUID用于事件追踪
        return Result.success(requestId);
    }

    @PostMapping("/deploy-only")
    @Log(title = "仅部署")
    public Result<String> deployOnly(@RequestBody @Valid DeployOnlyRequest request, HttpServletRequest servletRequest) {
        String requestId = UUID.randomUUID().toString();
        BuildAndDeployDTO req = BuildAndDeployDTO.builder()
                .projectName(request.getProjectName())
                .tag(request.getTag())
                .environment(request.getEnvironment())
                .user(UserGetter.get(servletRequest))
                .requestId(requestId)
                .type(BuildAndDeployTypeEnum.DEPLOY_ONLY)
                .build();
        ThreadPool.executorService().submit(() -> {
            deployService.buildAndDeploy(req);
        });
        //返回一个UUID用于事件追踪
        return Result.success(requestId);
    }

    @GetMapping("/detail/{envName}/{projectName}")
    @Log(title = "查看当前环境所部署服务的详情")
    public Result<ProjectDeployInfoResponse> deployDetail(@PathVariable String envName, @PathVariable String projectName) {
        return deployService.deployDetail(projectName, envName);
    }


    @GetMapping("/detail/list")
    public Result<List<ProjectDeployInfoResponse>> deployDetailList(@RequestParam(required = false) String envName,@RequestParam(required = false) String projectName) throws InterruptedException {
        return deployService.deployDetailList(projectName,envName);
    }

    /**
     * 查询实时打包和部署进度
     *
     * @param requestId 请求id
     * @return 返回进度
     */
    @Log
    @GetMapping("/progress/{requestId}")
    public Result<BuildAndDeployProgressResponse> getBuildAndDeployDetail(@PathVariable String requestId) {
        return Result.success(Cache.get(requestId, BuildAndDeployProgressResponse.class));
    }

    @Autowired
    public void setDeployService(DeployService deployService) {
        this.deployService = deployService;
    }
}
