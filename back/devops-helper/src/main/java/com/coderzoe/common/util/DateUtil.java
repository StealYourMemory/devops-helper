package com.coderzoe.common.util;

import com.coderzoe.common.exception.CommonException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author yinhuasheng
 * @date 2024/8/19 11:37
 */
public class DateUtil {
    public static Date formatDate(String date) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy, h:mm a zzz", Locale.ENGLISH);
            return formatter.parse(date);
        } catch (ParseException e) {
            throw new CommonException("日期解析异常");
        }
    }

    public static String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        return formatter.format(date);
    }
}
