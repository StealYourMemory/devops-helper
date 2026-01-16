package com.coderzoe.common.enums;

/**
 * 设备状态
 *
 * @author yinhuasheng
 * @date 2024/8/19 16:43
 */
public enum DeviceStatus {
    /**
     * 初始，连接中，断连和已死亡/销毁
     */
    INIT,
    CONNECTING,
    AUTH_FAIL,
    DISCONNECT,
    DEATH
}
