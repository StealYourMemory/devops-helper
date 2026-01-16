package com.coderzoe.component.k8s.service;

import com.coderzoe.common.Result;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.common.util.Cache;
import com.coderzoe.component.k8s.config.K8sConfig;
import com.coderzoe.component.k8s.device.Device;
import com.coderzoe.component.k8s.device.DeviceManage;
import com.coderzoe.component.k8s.model.K8sEnvAndServers;
import com.coderzoe.component.k8s.model.request.CmdRequest;
import com.coderzoe.component.k8s.model.request.ScpRequest;
import com.coderzoe.component.k8s.model.request.UpdateImageVersionRequest;
import com.coderzoe.component.k8s.model.response.CmdResponse;
import com.coderzoe.config.ProjectConfig;
import com.coderzoe.model.pojo.Project;
import com.coderzoe.model.response.BuildAndDeployProgressResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.coderzoe.common.Constants.IMAGE_TAG_SPLIT;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:08
 */
@Service
@Slf4j
@Setter(onMethod = @__({@Autowired}))
public class K8sCmdService {
    private ProjectConfig projectConfig;
    private K8sConfig k8sConfig;

    /**
     * 更新镜像版本
     *
     * @return 返回是否成功
     */
    public Result<CmdResponse> updateImageVersion(UpdateImageVersionRequest request) {
        Project project = projectConfig.get(request.getProjectName());
        K8sEnvAndServers envAndServers = DeviceManage.get(request.getEnvironment());
        String updateCmd = k8sConfig.getUpdateDeploymentVersionCmd(project.getK8sDeployment(), project.getK8sDeploymentImage(), request.getTag());
        Device device = envAndServers.getAnyOrThrow();
        switch (envAndServers.getEnvType()) {
            case NEED_DOCKER_PULL -> {
                Result<CmdResponse> result = handleDockerPull(request, project);
                if (!result.isSuccess()) {
                    return result;
                }
                return sendCmd(updateCmd, device);
            }
            case REBIRTH_V2 -> {
                Result<CmdResponse> result = handleRebirthV2(request, project);
                if (!result.isSuccess()) {
                    return result;
                }
                return sendCmd(updateCmd, device);
            }
            case REBIRTH_V1 -> {
                return sendCmd(updateCmd, device);
            }
            default -> throw new CommonException("未知配置:envType" + envAndServers.getEnvType());
        }
    }

    /**
     * 查询当前正部署的详情(其实就是执行kubectl get pod | grep {podName})
     *
     * @return 返回部署详情
     */
    public Result<CmdResponse> getUpdateStatus(UpdateImageVersionRequest request) {
        Project project = projectConfig.get(request.getProjectName());
        String cmd = k8sConfig.getStatusGetCmd(project.getK8sDeployment());
        return sendCmd(cmd, DeviceManage.get(request.getEnvironment()).getAnyOrThrow());
    }


    /**
     * 返回当前项目在某环境下的tag
     */
    public Result<String> getProjectTag(String projectName, String envName) {
        Project project = projectConfig.get(projectName);
        String cmd = k8sConfig.getTagGetCmd(project.getK8sDeployment());
        Result<CmdResponse> cmdResponseResult = sendCmd(cmd, DeviceManage.get(envName).getAnyOrThrow());
        if (!cmdResponseResult.isSuccess()) {
            return Result.fail(cmdResponseResult.getMsg());
        }
        String response = cmdResponseResult.getData().getResponse();
        if (StringUtils.hasLength(response) && response.split(IMAGE_TAG_SPLIT).length > 1) {
            return Result.success(response.split(IMAGE_TAG_SPLIT)[1]);
        }
        return Result.fail("解析tag失败，tag信息：" + response);

    }

    private Result<CmdResponse> handleDockerPull(UpdateImageVersionRequest request, Project project) {
        K8sEnvAndServers envAndServers = DeviceManage.get(request.getEnvironment());
        String pullImageCmd = k8sConfig.getDockerPullImageCmd(project.getK8sDeploymentImage(), request.getTag());
        BuildAndDeployProgressResponse deployProgress = Cache.get(request.getRequestId(), BuildAndDeployProgressResponse.class);
        assert deployProgress != null;
        deployProgress.setDeployProgress(new ArrayList<>());
        //去每个服务器上执行docker pull，将镜像拉下来
        for (Device device : envAndServers.getDevices()) {
            log.info("服务器{}执行指令{}拉取docker镜像", device, pullImageCmd);
            updateProgress(request.getRequestId(), "去服务器" + device + "执行指令" + pullImageCmd + "拉取docker镜像");

            Result<CmdResponse> result = sendCmd(pullImageCmd, device);
            if (!result.isSuccess()) {
                log.error("服务器{}执行指令{}拉取docker镜像失败，失败原因:{}", device, pullImageCmd, result.getMsg());
                return result;
            }
        }
        return Result.success(null);
    }

    /**
     * 对于rebirth2.0的服务，由于k8s环境本身无法直接拉取我们jenkins打包后入的harbor镜像仓库，因此需要做如下操作：
     * 1. 在一个母环境上拉取之前打包的镜像，要求母环境是rebirth1.0的环境(能使用docker指令且能拉取jenkins打包后的镜像)
     * 2. 将母环境拉取的镜像保存成文件到本地
     * 3. 通过scp将母环境本地的镜像传入到要部署的环境的其中一台k8s服务器上
     * 4. 在要部署的k8s服务器上加载传输过来的镜像
     * 5. 将加载后的镜像传入到要部署的环境的服务器对应的harbor仓库
     * 6. 更新k8s yaml实现版本更新
     */
    private Result<CmdResponse> handleRebirthV2(UpdateImageVersionRequest request, Project project) {
        try {
            int index = 0;
            String requestId = request.getRequestId();
            String image = project.getK8sDeploymentImage();
            String tag = request.getTag();
            String projectName = request.getProjectName();

            Device srcDevice = DeviceManage.get(k8sConfig.getBaseEnv()).getByIpOrThrow(k8sConfig.getBaseDeviceIp());
            Device dstDevice = DeviceManage.get(request.getEnvironment()).getAnyOrThrow();

            //建目录
            String mkdirCmd = k8sConfig.getMkdirCmd();
            sendCmd(mkdirCmd, srcDevice);
            sendCmd(mkdirCmd, dstDevice);

            //母环境拉镜像
            String dockerPullImageCmd = k8sConfig.getDockerPullImageCmd(image, tag);
            updateProgress(requestId, ++index + ". 在设备" + srcDevice + "执行指令" + dockerPullImageCmd + "拉取docker镜像");
            Result<CmdResponse> pullResult = sendCmd(dockerPullImageCmd, srcDevice);
            if (!pullResult.isSuccess()) {
                return Result.fail("设备:" + srcDevice + "拉取镜像" + dockerPullImageCmd + "失败，失败原因:" + pullResult.getMsg());
            }

            //母环境存镜像
            String dockerSaveCmd = k8sConfig.getDockerSaveCmd(projectName, image, tag);
            updateProgress(requestId, ++index + ". 在设备" + srcDevice + "执行指令" + dockerSaveCmd + "保存docker镜像");
            Result<CmdResponse> saveResult = sendCmd(dockerSaveCmd, srcDevice);
            if (!saveResult.isSuccess()) {
                return Result.fail("设备:" + srcDevice + "保存镜像" + dockerSaveCmd + "失败，失败原因:" + saveResult.getMsg());
            }
            //这里加一点休眠 因为偶然会出现母环境拉完镜像后，立即读取读不到的情况
            TimeUnit.SECONDS.sleep(5);

            //scp拷贝文件
            String path = k8sConfig.getSaveImagePath(projectName, tag);
            updateProgress(requestId, ++index + ". 设备" + srcDevice + "向设备" + dstDevice + "通过scp传输镜像" + path + "当前步骤可能比较耗时");
            Result<CmdResponse> scpResult = dstDevice.scpSend(new ScpRequest(path, path, srcDevice, dstDevice));
            if (!scpResult.isSuccess()) {
                return Result.fail("设备:" + srcDevice + "向设备" + dstDevice + "传输镜像" + path + "失败，失败原因:" + scpResult.getMsg());
            }

            //去目的k8s上加载和上传镜像
            String loadImageCmd = k8sConfig.getNerdctlLoadImageCmd(projectName, tag);
            updateProgress(requestId, ++index + ". 在设备" + dstDevice + "执行指令" + loadImageCmd + "加载docker镜像");
            Result<CmdResponse> loadResult = sendCmd(loadImageCmd, dstDevice);
            if (!loadResult.isSuccess()) {
                return Result.fail("设备:" + dstDevice + "加载镜像" + loadImageCmd + "失败，失败原因:" + loadResult.getMsg());
            }

            //在目的k8s上push镜像到它的中央仓库
            String pushImageCmd = k8sConfig.getNerdctlPushImageCmd(image, tag);
            updateProgress(requestId, ++index + ". 在设备" + dstDevice + "执行指令" + pushImageCmd + "将镜像推送到中央仓库");
            Result<CmdResponse> pushResult = sendCmd(pushImageCmd, dstDevice);
            if (!pushResult.isSuccess()) {
                return Result.fail("设备:" + dstDevice + "push镜像" + pushResult + "失败，失败原因:" + pushResult.getMsg());
            }

            //镜像包没用了，删除镜像包
            updateProgress(requestId, ++index + ". 在设备" + dstDevice + "和" + dstDevice + "清除镜像文件" + path);
            String rmFileCmd = k8sConfig.getRmFileCmd(path);
            sendCmd(rmFileCmd, srcDevice);
            sendCmd(rmFileCmd, dstDevice);

            return Result.success(null);

        } catch (Exception e) {
            log.error("K8sCmdService#handleRebirthV2执行失败:", e);
            throw new CommonException(e);
        }
    }


    private Result<CmdResponse> sendCmd(String cmd, Device device) {
        return device.send(new CmdRequest(cmd));
    }

    private static void updateProgress(String requestId, String message) {
        BuildAndDeployProgressResponse deployProgress = Cache.get(requestId, BuildAndDeployProgressResponse.class);
        assert deployProgress != null;
        if (deployProgress.getDeployProgress() == null) {
            deployProgress.setDeployProgress(new ArrayList<>());
        }
        deployProgress.getDeployProgress().add(message);
        Cache.set(requestId, deployProgress);
    }
}
