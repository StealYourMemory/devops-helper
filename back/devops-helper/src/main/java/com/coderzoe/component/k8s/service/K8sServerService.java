package com.coderzoe.component.k8s.service;

import com.coderzoe.common.exception.CommonException;
import com.coderzoe.component.k8s.device.Device;
import com.coderzoe.component.k8s.device.DeviceBuild;
import com.coderzoe.component.k8s.device.DeviceManage;
import com.coderzoe.component.k8s.model.K8sEnvAndServerInfo;
import com.coderzoe.component.k8s.model.K8sEnvAndServers;
import com.coderzoe.component.k8s.model.database.K8sEnvironmentDO;
import com.coderzoe.component.k8s.model.database.K8sServerDO;
import com.coderzoe.component.k8s.model.request.AddServerRequest;
import com.coderzoe.component.k8s.model.request.DeleteServerRequest;
import com.coderzoe.component.k8s.repository.K8sEnvironmentRepository;
import com.coderzoe.component.k8s.repository.K8sServerRepository;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yinhuasheng
 * @date 2024/8/19 17:06
 */
@Service
@Setter(onMethod = @__(@Autowired))
public class K8sServerService {
    private K8sEnvironmentRepository k8sEnvironmentRepository;
    private K8sServerRepository k8sServerRepository;

    public List<K8sEnvAndServerInfo> getEnvAndServerList() {
        List<K8sEnvironmentDO> envList = k8sEnvironmentRepository.findAll();
        List<K8sServerDO> serverList = k8sServerRepository.findAll();
        return envList.stream().map(env -> {
            List<K8sServerDO> envServerList = serverList.stream()
                    .filter(server -> Objects.equals(server.getEnvironmentId(), env.getId()))
                    .toList();
            return new K8sEnvAndServerInfo(env, envServerList);
        }).toList();
    }

    /**
     * 创建环境和设备
     *
     * @param envAndServerInfo 环境和设备信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void createEnvAndServer(K8sEnvAndServerInfo envAndServerInfo) throws Exception {
        //0. 参数校验
        if (k8sEnvironmentRepository.findByName(envAndServerInfo.getEnvName()).isPresent()) {
            throw new CommonException("存在环境" + envAndServerInfo.getEnvName());
        }
        if (envAndServerInfo.getServerSet() != null && !envAndServerInfo.getServerSet().isEmpty()) {
            int size = envAndServerInfo.getServerSet().stream().map(p -> p.getIp() + ":" + p.getPort()).collect(Collectors.toSet()).size();
            if (envAndServerInfo.getServerSet().size() != size) {
                throw new CommonException("存在相同ip和端口的服务");
            }
        }

        //1. 创建环境
        K8sEnvironmentDO env = k8sEnvironmentRepository.save(new K8sEnvironmentDO(envAndServerInfo));

        //2. 创建设备
        if (envAndServerInfo.getServerSet() != null && !envAndServerInfo.getServerSet().isEmpty()) {
            k8sServerRepository.saveAll(envAndServerInfo.getServerSet()
                    .stream()
                    .map(p -> new K8sServerDO(p, env.getId()))
                    .toList());

            //3. 创建设备连接
            this.createServerConnect(envAndServerInfo);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void addServer(AddServerRequest request) throws Exception {
        K8sEnvAndServers envAndServers = DeviceManage.get(request.getEnvName());
        Optional<K8sEnvironmentDO> envOptional = k8sEnvironmentRepository.findByName(request.getEnvName());

        //参数校验
        if (envAndServers == null || envOptional.isEmpty()) {
            throw new CommonException("未找到环境" + request.getEnvName());
        }
        Optional<Device> first = envAndServers.getDevices()
                .stream()
                .filter(p -> Objects.equals(p.metaData().getIp(), request.getServerInfo().getIp()) && Objects.equals(p.metaData().getPort(), request.getServerInfo().getPort()))
                .findFirst();
        if (first.isPresent()) {
            throw new CommonException(first.get() + "的服务器已经存在，不要重复添加");
        }
        //入库
        k8sServerRepository.save(new K8sServerDO(request.getServerInfo(), envOptional.get().getId()));
        //建连接并注册
        envAndServers.registerDevice(createServer(request.getEnvName(), request.getServerInfo()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteEnv(String envName) {
        Optional<K8sEnvironmentDO> envOptional = k8sEnvironmentRepository.findByName(envName);
        if (envOptional.isEmpty()) {
            throw new CommonException("未找到环境" + envName);
        }
        //删库
        k8sEnvironmentRepository.delete(envOptional.get());
        k8sServerRepository.deleteByEnvironmentId(envOptional.get().getId());
        //删连接 清内存
        DeviceManage.unregister(envName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteServer(DeleteServerRequest request) {
        Optional<K8sEnvironmentDO> envOptional = k8sEnvironmentRepository.findByName(request.getEnvName());
        if (envOptional.isEmpty()) {
            throw new CommonException("未找到环境" + request.getEnvName());
        }
        Optional<K8sServerDO> serverOptional = k8sServerRepository.findByEnvironmentIdAndIpAndPort(envOptional.get().getId(), request.getIp(), request.getPort());
        if (serverOptional.isEmpty()) {
            throw new CommonException("环境" + request.getEnvName() + "下未找到设备" + request.getIp() + ":" + request.getPort());
        }
        //删库
        k8sServerRepository.delete(serverOptional.get());
        //删连接 清内存
        DeviceManage.get(request.getEnvName()).unregisterDevice(serverOptional.get().getIp(), serverOptional.get().getPort());
    }

    public void createServerConnect(K8sEnvAndServerInfo envAndServerInfo) throws Exception {
        Set<Device> deviceSet = new HashSet<>();
        for (K8sEnvAndServerInfo.K8sServerInfo server : envAndServerInfo.getServerSet()) {
            deviceSet.add(createServer(envAndServerInfo.getEnvName(), server));
        }
        DeviceManage.register(new K8sEnvAndServers(envAndServerInfo.getEnvName(),
                envAndServerInfo.getEnvDescription(),
                envAndServerInfo.getEnvType(),
                deviceSet));
    }

    private Device createServer(String envName, K8sEnvAndServerInfo.K8sServerInfo serverInfo) throws Exception {
        return DeviceBuild.builder()
                .environment(envName)
                .ip(serverInfo.getIp())
                .port(serverInfo.getPort())
                .userName(serverInfo.getUserName())
                .password(new String(Base64.getDecoder().decode(serverInfo.getPassword())))
                .build();
    }
}
