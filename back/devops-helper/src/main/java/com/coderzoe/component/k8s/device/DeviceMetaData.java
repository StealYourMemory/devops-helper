package com.coderzoe.component.k8s.device;

import lombok.Data;

/**
 * @author yinhuasheng
 * @date 2024/8/19 16:44
 */
@Data
public class DeviceMetaData {
    private String ip;
    private int port;
    private String userName;
    private String password;
    private String environment;

    public static DeviceMetaData defaultMetaData() {
        DeviceMetaData deviceMetaData = new DeviceMetaData();
        deviceMetaData.port = 22;
        deviceMetaData.userName = "root";
        return deviceMetaData;
    }
}
