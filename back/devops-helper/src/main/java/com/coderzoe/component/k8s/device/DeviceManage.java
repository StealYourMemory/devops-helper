package com.coderzoe.component.k8s.device;

import com.coderzoe.component.k8s.model.K8sEnvAndServers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yinhuasheng
 * @date 2024/8/19 16:58
 */
public class DeviceManage {
    private static final Map<String, K8sEnvAndServers> ENV_AND_DEVICE_MAP;

    static {
        ENV_AND_DEVICE_MAP = new ConcurrentHashMap<>(8);
    }

    public synchronized static void register(K8sEnvAndServers envAndServers) {
        ENV_AND_DEVICE_MAP.put(envAndServers.getEnvName(), envAndServers);
    }

    public synchronized static void unregister(String environmentName) {
        K8sEnvAndServers envAndServers = ENV_AND_DEVICE_MAP.get(environmentName);
        envAndServers.destroy();
        ENV_AND_DEVICE_MAP.remove(environmentName);
    }

    public synchronized static K8sEnvAndServers get(String environmentName) {
        return ENV_AND_DEVICE_MAP.get(environmentName);
    }
}
