package com.coderzoe.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author yinhuasheng
 * @date 2024/8/19 14:26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginCookie {
    private List<String> cookies;

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String cookie : this.cookies) {
            result.append(cookie).append(";");
        }
        return result.toString();
    }
}
