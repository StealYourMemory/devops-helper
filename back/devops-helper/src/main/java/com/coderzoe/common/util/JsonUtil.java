package com.coderzoe.common.util;

import com.coderzoe.common.exception.CommonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 基于jackson封装的JSON工具类
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:33
 */
public class JsonUtil {
    /**
     * {@link ObjectMapper}本身是线程安全的，因此全局唯一就够了，初始化的时候建好
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 忽略JSON中未知的字段
        OBJECT_MAPPER.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJsonString(Object target) {
        try {
            return OBJECT_MAPPER.writeValueAsString(target);
        } catch (JsonProcessingException e) {
            throw new CommonException(e);
        }
    }

    public static <T> T parseObject(String json, Class<T> tClass) {
        try {
            return OBJECT_MAPPER.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            throw new CommonException(e);
        }
    }

    public static <T> List<T> parseToListObject(String json, TypeReference<List<T>> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new CommonException(e);
        }
    }


    public static JsonNode parseObject(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new CommonException(e);
        }
    }

    public static String getField(String json, String field) {
        try {
            return OBJECT_MAPPER.readTree(json).get(field).asText();
        } catch (JsonProcessingException e) {
            throw new CommonException(e);
        }
    }
}
