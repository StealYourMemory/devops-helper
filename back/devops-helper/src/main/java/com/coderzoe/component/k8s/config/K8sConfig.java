package com.coderzoe.component.k8s.config;

import com.coderzoe.component.k8s.model.K8sServerConnectConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yinhuasheng
 * @date 2024/8/19 15:34
 */
@ConfigurationProperties(prefix = "k8s")
@Configuration
@Setter
public class K8sConfig {
    @Getter
    private String k8sNameSpace;

    @Getter
    private String saveImagePath;
    @Getter
    private String saveProxyPath;
    @Getter
    private String baseEnv;
    @Getter
    private String baseDeviceIp;
    @Getter
    private K8sServerConnectConfig connectConfig = new K8sServerConnectConfig();

    private String dockerPullImageCmd = "docker pull %s:%s";
    private String dockerSaveCmd = "docker save -o %s %s";

    private String updateDeploymentVersionCmd = "kubectl patch deployment %s --type='json' -p='[{\"op\": \"replace\", \"path\": \"/spec/template/spec/containers/0/image\", \"value\":\"%s:%s\"}]' -n %s";
    private String statusGetCmd = "kubectl get pod -n %s | grep %s";
    private String rmFileCmd = "rm -f %s ";
    private String mkdirCmd = "mkdir -p %s ";

    private String nerdctlLoadImageCmd = "nerdctl load -i %s";
    private String nerdctlPushImageCmd = "nerdctl push %s --insecure-registry";


    private String tagGetCmd = "kubectl get deployment -n %s %s -o=jsonpath='{.spec.template.spec.containers[*].image}'";


    public String getDockerPullImageCmd(String imageId, String tag) {
        return String.format(dockerPullImageCmd, imageId, tag);
    }

    public String getUpdateDeploymentVersionCmd(String deploymentName, String imageId, String tag) {
        return String.format(updateDeploymentVersionCmd, deploymentName, imageId, tag, k8sNameSpace);
    }

    public String getStatusGetCmd(String projectName) {
        return String.format(statusGetCmd, k8sNameSpace, projectName);
    }

    public String getDockerSaveCmd(String projectName, String imageId, String tag) {
        String savePath = getSaveImagePath(projectName, tag);
        String imageTag = imageId + ":" + tag;
        return String.format(dockerSaveCmd, savePath, imageTag);
    }

    public String getSaveImagePath(String projectName, String tag) {
        return this.saveImagePath + "dh-" + projectName + "_" + tag + ".tar";
    }

    public String getMkDirSaveProxyPath() {
        return String.format(mkdirCmd, saveProxyPath);
    }

    public String getRmFileCmd(String filePath) {
        return String.format(rmFileCmd, filePath);
    }

    public String getMkdirCmd() {
        return String.format(mkdirCmd, saveImagePath);
    }

    public String getNerdctlLoadImageCmd(String projectName, String tag) {
        String path = getSaveImagePath(projectName, tag);
        return String.format(nerdctlLoadImageCmd, path);
    }

    public String getNerdctlPushImageCmd(String imageId, String tag) {
        return String.format(nerdctlPushImageCmd, imageId + ":" + tag);
    }
    public String getTagGetCmd(String projectDeploymentName) {
        return String.format(tagGetCmd, k8sNameSpace,projectDeploymentName);
    }
}
