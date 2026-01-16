package com.coderzoe.component.seafile.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/20 10:39
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeafileToken {
    private String csrfMiddlewareToken;
    private List<String> cookies;

    public String getCookie() {
        StringBuilder result = new StringBuilder();
        for (String cookie : this.cookies) {
            result.append(cookie).append(";");
        }
        return result.toString();
    }
}
