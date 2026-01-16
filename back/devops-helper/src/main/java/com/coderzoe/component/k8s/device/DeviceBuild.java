package com.coderzoe.component.k8s.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author yinhuasheng
 * @date 2024/8/19 16:57
 */
@Component
public class DeviceBuild {
    private static DeviceFactory deviceFactory;

    public static DeviceBuilder builder() {
        return new DeviceBuilder(deviceFactory);
    }

    @Autowired
    public void setDeviceFactory(DeviceFactory deviceFactory) {
        DeviceBuild.deviceFactory = deviceFactory;
    }
}
