package com.coderzoe.service;

import com.coderzoe.common.Constants;
import com.coderzoe.common.Result;
import com.coderzoe.common.ThreadPool;
import com.coderzoe.common.enums.BuildStatus;
import com.coderzoe.common.util.Cache;
import com.coderzoe.common.util.JsonUtil;
import com.coderzoe.component.jenkins.model.BuildHistory;
import com.coderzoe.component.jenkins.model.request.BuildRequest;
import com.coderzoe.component.jenkins.service.JenkinsBuildService;
import com.coderzoe.component.k8s.model.K8sEnvAndServerInfo;
import com.coderzoe.component.k8s.model.request.UpdateImageVersionRequest;
import com.coderzoe.component.k8s.model.response.CmdResponse;
import com.coderzoe.component.k8s.service.K8sCmdService;
import com.coderzoe.component.k8s.service.K8sServerService;
import com.coderzoe.config.ProjectConfig;
import com.coderzoe.model.database.OperationLogDO;
import com.coderzoe.model.pojo.Project;
import com.coderzoe.model.request.BuildAndDeployDTO;
import com.coderzoe.model.response.BuildAndDeployProgressResponse;
import com.coderzoe.model.response.ProjectDeployInfoResponse;
import com.coderzoe.repository.OperationLogRepository;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.coderzoe.common.Constants.WAIT_DEPLOY_MAX_TIME;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:55
 */
@Slf4j
@Service
@Setter(onMethod = @__(@Autowired))
public class DeployService {
    private JenkinsBuildService jenkinsBuildService;
    private K8sCmdService k8sCmdService;
    private OperationLogRepository operationLogRepository;
    private ProjectConfig projectConfig;
    private K8sServerService k8sServerService;

    public DeployService(JenkinsBuildService jenkinsBuildService, K8sCmdService k8sCmdService) {
        this.jenkinsBuildService = jenkinsBuildService;
        this.k8sCmdService = k8sCmdService;
    }

    /**
     * 打包和部署的统一实现方法 可以仅打包，也可以仅部署
     */
    public boolean buildAndDeploy(BuildAndDeployDTO request) {
        //记录日志
        OperationLogDO operationLog = OperationLogDO.builder()
                .user(request.getUser())
                .requestId(request.getRequestId())
                .operation(request.getType().getDescription())
                .requestDetail(JsonUtil.toJsonString(request))
                .startTime(new Date())
                .build();
        //插入日志
        operationLogRepository.save(operationLog);

        String requestId = request.getRequestId();
        Cache.set(requestId, new BuildAndDeployProgressResponse(requestId));


        StringBuilder stringBuilder = new StringBuilder();
        try {
            boolean allSuccess = true;
            if (request.needBuild()) {
                Result<BuildHistory> buildResult = build(request);
                if (buildResult.isSuccess()) {
                    stringBuilder.append("打包成功，打包详情：").append(JsonUtil.toJsonString(buildResult.getData()));
                } else {
                    allSuccess = false;
                    stringBuilder.append("打包失败：").append(buildResult.getMsg());
                }
            }
            if (allSuccess && request.needDeploy()) {
                Result<String> deployResult = deploy(request);
                if (deployResult.isSuccess()) {
                    stringBuilder.append("\n部署完成");
                } else {
                    allSuccess = false;
                    stringBuilder.append("\n部署失败：").append(deployResult.getMsg());
                }
            }
            operationLog.setSuccess(allSuccess);
            operationLog.setResultDetail(stringBuilder.toString());
            operationLog.setEndTime(new Date());
            updateLog(operationLog);
            return allSuccess;
        } catch (Exception e) {
            log.error("异常DeployService#buildAndDeploy", e);
            operationLog.setSuccess(false);
            operationLog.setResultDetail(e.getMessage());
            operationLog.setEndTime(new Date());
            updateLog(operationLog);
            return false;
        } finally {
            //一段时间后移除缓存，不立马移除
            ThreadPool.scheduledExecutorService().schedule(() -> Cache.remove(requestId), 30, TimeUnit.SECONDS);
        }
    }

    /**
     * 查询某环境下某项目的当前正部署的版本、时间、分支等信息
     *
     * @return 返回部署详情
     */
    public Result<ProjectDeployInfoResponse> deployDetail(String projectName, String envName) {
        //先查询环境上该项目部署的tag
        Result<String> getTagResult = k8sCmdService.getProjectTag(projectName, envName);
        if (!getTagResult.isSuccess()) {
            return Result.fail(getTagResult.getMsg());
        }
        //再根据项目和tag去jenkins上查询当初详细的打包参数
        Result<List<BuildHistory>> jenkinsResult = jenkinsBuildService.getBuildHistory(projectName, getTagResult.getData());
        if (!jenkinsResult.isSuccess() || CollectionUtils.isEmpty(jenkinsResult.getData())) {
            return Result.fail(jenkinsResult.getMsg());
        }
        return Result.success(new ProjectDeployInfoResponse(jenkinsResult.getData().getFirst(), envName));
    }

    public Result<List<ProjectDeployInfoResponse>> deployDetailList(String projectName, String envName) throws InterruptedException {
        Set<Project> projects = projectConfig.getProjects();
        List<K8sEnvAndServerInfo> envList = k8sServerService.getEnvAndServerList();
        //注：我们这种场景其实不适合cowList，因为是纯写场景，但我又懒得换为性能更强的容器或加锁
        List<ProjectDeployInfoResponse> list = new CopyOnWriteArrayList<>();
        if (StringUtils.hasLength(projectName)) {
            projects = projects.stream().filter(p -> projectName.equals(p.getName())).collect(Collectors.toSet());
        }
        if (StringUtils.hasLength(envName)) {
            envList = envList.stream().filter(p -> envName.equals(p.getEnvName())).toList();
        }
        if (projects.isEmpty() || envList.isEmpty()) {
            return Result.fail("未找到要查询的环境或项目");
        }
        CountDownLatch countDownLatch = new CountDownLatch(envList.size());
        for (K8sEnvAndServerInfo env : envList) {
            Set<Project> finalProjects = projects;
            ThreadPool.executorService().submit(() -> {
                try {
                    for (Project project : finalProjects) {
                        Result<ProjectDeployInfoResponse> result = this.deployDetail(project.getName(), env.getEnvName());
                        if (result.isSuccess()) {
                            list.add(result.getData());
                        } else {
                            log.error("异常环境:{},异常服务：{}", env.getEnvName(), project.getName());
                        }
                    }

                } finally {
                    countDownLatch.countDown();
                }
            });


        }
        //noinspection ResultOfMethodCallIgnored
        countDownLatch.await(60, TimeUnit.SECONDS);

        //异步任务排个序
        list.sort(Comparator.comparing(ProjectDeployInfoResponse::getEnvName).thenComparing(ProjectDeployInfoResponse::getProjectName));
        return Result.success(list);
    }

    public Result<BuildHistory> build(BuildAndDeployDTO request) {
        //记录实时日志
        String requestId = request.getRequestId();
        BuildAndDeployProgressResponse response = Cache.get(requestId, BuildAndDeployProgressResponse.class);
        assert response != null;
        BuildRequest buildRequest = BuildRequest.builder()
                .projectName(request.getProjectName())
                .branch(request.getBranch())
                .tag(request.getTag())
                .build();
        Result<Void> buildResult = jenkinsBuildService.build(buildRequest);

        //构建失败了 直接结束
        if (!buildResult.isSuccess()) {
            response.setBuildProgress(Result.fail(buildResult.getMsg()));
            //更新结果
            Cache.set(requestId, response);
            return Result.fail(buildResult.getMsg());
        }

        //jenkins打包是一个异步的流程 我们需要主动改为同步，也即等待打包完成再部署，如果打包失败就不部署
        //这里一直轮询查询当前打包的进度
        long startTime = System.currentTimeMillis();
        BuildHistory buildHistory = null;
        while (buildHistory == null || buildHistory.getStatus() == BuildStatus.Pending) {
            try {
                //加个最大时长等待 避免死轮询
                if (System.currentTimeMillis() - startTime > Constants.WAIT_BUILD_MAX_TIME) {
                    String failMessage = "等待打包时长超时，目前已经等待超过3分钟，打包仍未完成";
                    response.setBuildProgress(Result.fail(failMessage));
                    Cache.set(requestId, response);
                    return Result.fail(failMessage);
                }
                TimeUnit.SECONDS.sleep(3);
                //根据本次打包的tag查询结果
                Result<List<BuildHistory>> result = jenkinsBuildService.getBuildHistory(request.getProjectName(), request.getTag());
                if (result.isSuccess()) {
                    buildHistory = result.getData().getFirst();
                    //实时更新进度
                    response.setBuildProgress(Result.success(buildHistory));
                    Cache.set(requestId, response);
                }
            } catch (Exception e) {
                log.error("DeployService#build 异常", e);
            }
        }
        return buildHistory.getStatus() == BuildStatus.Success ? Result.success(buildHistory) : Result.fail(JsonUtil.toJsonString(buildHistory));
    }

    /**
     * 部署 这里的日志信息可能完全不够，但如果想详细加日志，就得侵入到业务代码，暂时先不加
     * 2024-08-19 新增部署的详细日志 侵入到业务代码的那种
     */
    public Result<String> deploy(BuildAndDeployDTO request) {
        String requestId = request.getRequestId();
        BuildAndDeployProgressResponse response = Cache.get(requestId, BuildAndDeployProgressResponse.class);
        assert response != null;

        UpdateImageVersionRequest updateImageVersionRequest = UpdateImageVersionRequest.builder()
                .projectName(request.getProjectName())
                .environment(request.getEnvironment())
                .tag(request.getTag())
                .requestId(requestId)
                .build();
        Result<CmdResponse> result = k8sCmdService.updateImageVersion(updateImageVersionRequest);
        //目前其实暂时无法知道k8s 是否部署成功，因此一个简单的版本是不断的get pod，然后将结果返回给页面，让用户自己判断是否部署成功
        if (result.isSuccess()) {
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < WAIT_DEPLOY_MAX_TIME) {
                try {
                    Result<CmdResponse> updateStatus = k8sCmdService.getUpdateStatus(updateImageVersionRequest);
                    if (updateStatus.isSuccess()) {
                        response.setDeployProgress(Collections.singletonList(updateStatus.getData().getResponse()));
                        Cache.set(requestId, response);
                    }
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    log.error("DeployService#deploy 异常", e);
                }
            }
        } else {
            response.setDeployProgress(Collections.singletonList("部署失败，失败原因：" + result.getMsg()));
            Cache.set(requestId, response);
            return Result.fail(result.getMsg());
        }
        return Result.success(null);
    }

    private void updateLog(OperationLogDO operationLog) {
        operationLogRepository.save(operationLog);
    }
}
