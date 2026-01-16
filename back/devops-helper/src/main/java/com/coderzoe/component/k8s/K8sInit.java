package com.coderzoe.component.k8s;

import com.coderzoe.component.k8s.config.K8sInitEnvAndServerConfig;
import com.coderzoe.component.k8s.model.K8sEnvAndServerInfo;
import com.coderzoe.component.k8s.model.database.K8sEnvironmentDO;
import com.coderzoe.component.k8s.model.database.K8sServerDO;
import com.coderzoe.component.k8s.repository.K8sEnvironmentRepository;
import com.coderzoe.component.k8s.repository.K8sServerRepository;
import com.coderzoe.component.k8s.service.K8sServerService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:04
 */
@Component
@Setter(onMethod = @__(@Autowired))
public class K8sInit implements ApplicationRunner {
    private K8sEnvironmentRepository k8sEnvironmentRepository;
    private K8sServerRepository k8sServerRepository;
    private K8sInitEnvAndServerConfig k8sInitEnvAndServerConfig;
    private K8sServerService k8sServerService;

    /**
     * 项目启动时 从数据库中查询数据并创建设备连接
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(ApplicationArguments args) throws Exception {
        initEnvAndServerDb();
        initDeviceConnect();
    }

    /**
     * 初始化环境
     */
    private void initEnvAndServerDb() {
        //如果初始化的时候库里记录为空，则向库里插入配置里初始化的环境
        if (k8sEnvironmentRepository.count() == 0 && k8sInitEnvAndServerConfig.getInitEnvAndServer() != null) {
            for (K8sEnvAndServerInfo envAndServerInfo : k8sInitEnvAndServerConfig.getInitEnvAndServer().values()) {
                K8sEnvironmentDO env = k8sEnvironmentRepository.save(new K8sEnvironmentDO(envAndServerInfo));
                List<K8sServerDO> serverList = envAndServerInfo.getServerSet().stream().map(p -> new K8sServerDO(p, env.getId())).toList();
                k8sServerRepository.saveAll(serverList);
            }
        }

    }

    /**
     * 初始化设备连接
     */
    private void initDeviceConnect() throws Exception {
        for (K8sEnvAndServerInfo envAndServerInfo : k8sServerService.getEnvAndServerList()) {
            k8sServerService.createServerConnect(envAndServerInfo);
        }
    }
}
