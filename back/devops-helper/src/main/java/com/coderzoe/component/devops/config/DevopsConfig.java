package com.coderzoe.component.devops.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;
import java.util.List;

import static com.coderzoe.common.Constants.DEVOPS_COMPANY_MEMBER_MAX_SIZE;

/**
 * @author yinhuasheng
 * @date 2024/8/19 18:01
 */
@ConfigurationProperties(prefix = "devops")
@Configuration
@Setter
public class DevopsConfig {
    @Getter
    private String baseUrl;
    @Getter
    private String projectId;
    @Getter
    private String userName;
    private String password;
    @Getter
    private String loginType = "external";
    private String loginUrl = "/api/v1.0/login";
    private String sprintOptionsUrl = "/api/v1.0/workware?Action=ListSprints&t=%d&projectId=%s";
    private String workIssueListUrl = "/api/v1.0/workware?Action=ListWorkIssues&t=%d&page=1&size=%d&projectId=%s&workflowId=2,3";
    private String workIssueListFilterBySprintsUrl = "&treeView=true&sprintIds=%s&allDemand=true";
    private String bugListUrl = "/api/v1.0/workware?Action=ListWorkIssues&t=%d&page=1&size=%d&projectId=%s&workflowId=1&treeView=false";
    private String bugListFilterBySprintUrl = "&sprintIds=%s&allBug=true";
    private String companyMemberUr = "/api/v1.0/mate?Action=GetCompanyMembers&t=%d&page=1&size=%d&name=&company_id=407668146862833523&child=false";

    public String getLoginUrl() {
        return baseUrl + loginUrl;
    }


    public String getPassword() {
        return new String(Base64.getDecoder().decode(password.getBytes()));
    }

    public String getSprintOptionsUrl() {
        return baseUrl + String.format(sprintOptionsUrl, System.currentTimeMillis(), projectId);
    }

    public String getWorkIssueListUrl() {
        return baseUrl + String.format(workIssueListUrl, System.currentTimeMillis(), Integer.MAX_VALUE, projectId);
    }

    public String getWorkIssueListFilterBySprintsUrl(List<String> sprints) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sprints.getFirst());
        for (int i = 1; i < sprints.size(); i++) {
            stringBuilder.append(",").append(sprints.get(i));
        }
        return getWorkIssueListUrl() + String.format(workIssueListFilterBySprintsUrl, stringBuilder);
    }

    public String getBugListUrl() {
        return baseUrl + String.format(bugListUrl, System.currentTimeMillis(), Integer.MAX_VALUE, projectId);
    }

    public String getBugListFilterBySprintsUrl(List<String> sprints) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(sprints.getFirst());
        for (int i = 1; i < sprints.size(); i++) {
            stringBuilder.append(",").append(sprints.get(i));
        }
        return getBugListUrl() + String.format(bugListFilterBySprintUrl, stringBuilder);
    }

    public String getCompanyMemberUr() {
        return baseUrl + String.format(companyMemberUr, System.currentTimeMillis(),DEVOPS_COMPANY_MEMBER_MAX_SIZE);
    }
}
