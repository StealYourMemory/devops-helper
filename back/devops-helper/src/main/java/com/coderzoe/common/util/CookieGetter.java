package com.coderzoe.common.util;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 提取Cookie的工具类
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:35
 */
public class CookieGetter {
    public static String get(String cookie) {
        return cookie.split(";", 2)[0];
    }

    public static List<String> get(ResponseEntity<?> response) {
        List<String> cookies = response.getHeaders().get("Set-Cookie");
        if (cookies != null) {
            return cookies.stream().map(CookieGetter::get).collect(Collectors.toList());
        }
        return null;
    }
}
