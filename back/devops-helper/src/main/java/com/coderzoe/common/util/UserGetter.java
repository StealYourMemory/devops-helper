package com.coderzoe.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 获取用户信息的，目前devops-helper并未增加用户/权限功能，但考虑到以后有加的概率，因此将用户信息获取统一抽象到这里
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:39
 */
public class UserGetter {
    public static String get(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null) {
            remoteAddr = forwarded.split(",")[0];
        }
        return remoteAddr;
    }
}
