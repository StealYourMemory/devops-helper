package com.coderzoe.component.k8s.device;

import com.coderzoe.common.exception.CommonException;

/**
 * @author yinhuasheng
 * @date 2024/8/19 16:57
 */
public class DeviceBuilder {
    private final DeviceFactory deviceFactory;
    private final DeviceMetaData deviceMetaData;

    public DeviceBuilder(DeviceFactory deviceFactory) {
        this.deviceFactory = deviceFactory;
        deviceMetaData = DeviceMetaData.defaultMetaData();
    }

    public DeviceBuilder ip(String ip) {
        deviceMetaData.setIp(ip);
        return this;
    }

    public DeviceBuilder port(int port) {
        deviceMetaData.setPort(port);
        return this;
    }

    public DeviceBuilder userName(String userName) {
        deviceMetaData.setUserName(userName);
        return this;
    }

    public DeviceBuilder password(String password) {
        deviceMetaData.setPassword(password);
        return this;
    }

    public DeviceBuilder environment(String environment) {
        deviceMetaData.setEnvironment(environment);
        return this;
    }

    public Device build() throws Exception {
        if (this.deviceMetaData.getEnvironment() == null || this.deviceMetaData.getIp() == null || this.deviceMetaData.getPassword() == null) {
            throw new CommonException("创建设备参数异常，参数不完整");
        }
        return deviceFactory.create(deviceMetaData);
    }
}
