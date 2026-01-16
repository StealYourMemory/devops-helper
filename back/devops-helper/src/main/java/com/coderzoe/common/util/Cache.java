package com.coderzoe.common.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内置一个全局cache 因为我们用的是sqlite库，性能比较弱鸡，增加缓存，避免频发查库
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:33
 */
public class Cache {
    public static Map<String, String> cache = new ConcurrentHashMap<>();

    public static String get(String key) {
        return cache.get(key);
    }

    public static <T> T get(String key, Class<T> clazz) {
        String value = get(key);
        if (value != null) {
            return JsonUtil.parseObject(value, clazz);
        }
        return null;
    }

    public static <T> List<T> getList(String key, TypeReference<List<T>> typeReference) {
        String value = get(key);
        if (value != null) {
            return JsonUtil.parseToListObject(value, typeReference);
        }
        return null;
    }


    public static void set(String key, String value) {
        cache.put(key, value);
    }

    public static void set(String key, Object value) {
        cache.put(key, JsonUtil.toJsonString(value));
    }

    public static void remove(String key) {
        cache.remove(key);
    }
}
