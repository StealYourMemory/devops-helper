package com.coderzoe.component.k8s.model;

import com.coderzoe.common.enums.EnvironmentTypeEnum;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.component.k8s.device.Device;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author yinhuasheng
 * @date 2024/8/19 16:06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class K8sEnvAndServers {
    private String envName;
    private String envDescription;
    private EnvironmentTypeEnum envType;
    private Set<Device> devices;

    public void registerDevice(Device... device) {
        if (Objects.isNull(devices)) {
            devices = new HashSet<>();
        }
        devices.addAll(Arrays.asList(device));
    }

    public void unregisterDevice(String ip, int port) {
        Optional<Device> device = devices.stream()
                .filter(p -> Objects.equals(p.metaData().getIp(), ip) && Objects.equals(p.metaData().getPort(), port))
                .findFirst();
        if (device.isPresent()) {
            device.get().destroy();
            devices.remove(device.get());
        }
    }

    public Optional<Device> getAny() {
        return devices.stream().findAny();
    }

    public Device getAnyOrThrow() {
        return this.getAny().orElseThrow(() -> new CommonException("环境" + this.getEnvDescription() + "下未找到任何K8s服务器"));
    }

    public Device getByIpOrThrow(String ip) {
        return this.devices
                .stream()
                .filter(p -> p.metaData().getIp().equals(ip))
                .findFirst()
                .orElseThrow(() -> new CommonException("环境" + this.getEnvDescription() + "下未找到ip为" + ip + "的K8s服务器"));
    }

    public void destroy() {
        if (Objects.nonNull(devices)) {
            devices.forEach(Device::destroy);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        K8sEnvAndServers that = (K8sEnvAndServers) o;
        return Objects.equals(envName, that.envName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(envName);
    }
}
