package com.coderzoe.component.jenkins.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * @author yinhuasheng
 * @date 2024/8/19 14:49
 */
@ConfigurationProperties(prefix = "jenkins")
@Configuration
@Setter
public class JenkinsConfig {
    @Getter
    private String baseUrl = "http://10.253.17.50";
    @Getter
    private String userName = "admin";
    private String password = "TTAwdmU=";

    private String initUrl = "/login?from=%2F";
    private String loginUrl = "/j_spring_security_check";
    @Getter
    private String historyUrl = "/job/%s/buildHistory/ajax";
    @Getter
    private String buildUrl = "/job/%s/build?delay=0sec";
    private String crumbUrl = "/crumbIssuer/api/json";
    private String historyParametersUrl = "/job/%s/%d/parameters";
    private String buildDetailUrl = "/job/%s/%d/console";

    public String getPassword() {
        return new String(Base64.getDecoder().decode(this.password));
    }

    public String getInitUrl() {
        return this.baseUrl + this.initUrl;
    }

    public String getLoginUrl() {
        return baseUrl + loginUrl;
    }

    public String getHistoryUrl(String projectUrl) {
        return baseUrl + String.format(historyUrl, projectUrl);
    }

    public String getBuildUrl(String projectUrl) {
        return baseUrl + String.format(buildUrl, projectUrl);
    }

    public String getCrumbUrl() {
        return baseUrl + crumbUrl;
    }

    public String getHistoryParametersUrl(String projectUrl, int buildId) {
        return baseUrl + String.format(historyParametersUrl, projectUrl, buildId);
    }

    public String getBuildDetailUrl(String projectUrl, int buildId) {
        return baseUrl + String.format(buildDetailUrl, projectUrl, buildId);
    }
}
