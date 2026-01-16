package com.coderzoe.component.k8s.device;

import com.coderzoe.component.k8s.config.K8sConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 设备工厂
 *
 * @author yinhuasheng
 * @date 2024/8/19 16:56
 */
@Component
public class DeviceFactory {
    private K8sConfig k8sConfig;

    public Device create(DeviceMetaData metaData) throws Exception {
        Device device = new Device(metaData, k8sConfig.getConnectConfig());
        device.connect();
        return device;
    }

    @Autowired
    public void setK8sConfig(K8sConfig k8sConfig) {
        this.k8sConfig = k8sConfig;
    }


}
