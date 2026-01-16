package com.coderzoe.component.seafile.model;

import com.coderzoe.common.enums.MatchType;
import com.coderzoe.common.exception.CommonException;
import lombok.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 归档名称匹配
 *
 * @author yinhuasheng
 * @date 2024/8/20 15:38
 */
@Data
public class ArchiveNameMatch {
    /**
     * 名称
     */
    private String name;
    /**
     * 匹配方式
     */
    private MatchType matchType;


    public boolean matches(String toMatchName) {
        if (matchType == null) {
            matchType = MatchType.CONTAINS;
        }
        switch (matchType) {
            case CONTAINS:
                return toMatchName.contains(name);
            case REGEX:
                Pattern pattern = Pattern.compile(name);
                Matcher matcher = pattern.matcher(toMatchName);
                return matcher.find();
            case EQUAL:
                return toMatchName.equals(name);
            default:
                throw new CommonException("未知匹配类型" + matchType);
        }
    }
}
