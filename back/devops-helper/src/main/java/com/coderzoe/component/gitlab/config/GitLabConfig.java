package com.coderzoe.component.gitlab.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * gitlab的配置
 *
 * @author yinhuasheng
 * @date 2024/8/19 14:05
 */
@ConfigurationProperties(prefix = "gitlab")
@Configuration
@Setter
public class GitLabConfig {
    @Getter
    private String baseUrl = "http://10.0.171.10";
    private String loginUrl = "/users/sign_in";
    private String authUrl = "/users/auth/ldapmain/callback";

    @Getter
    private String userName;

    private String password;

    @Getter
    private String authKeyRegex = "name=\"authenticity_token\" value=\"(.*?)\"";
    @Getter
    private String getBranchesUrl;


    public String getPassword() {
        return new String(Base64.getDecoder().decode(this.password));
    }

    public String getLoginUrl() {
        return this.baseUrl + this.loginUrl;
    }

    public String getAuthUrl() {
        return this.baseUrl + this.authUrl;
    }

    public String getGetBranchesUrl(String projectUrl) {
        return this.baseUrl + String.format(this.getBranchesUrl, projectUrl);
    }
}
