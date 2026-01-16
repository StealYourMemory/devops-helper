package com.coderzoe.controller;

import com.coderzoe.common.Result;
import com.coderzoe.common.log.Log;
import com.coderzoe.common.util.Cache;
import com.coderzoe.component.devops.model.DevopsCompanyMember;
import com.coderzoe.component.devops.model.DevopsSprintOptionResponse;
import com.coderzoe.component.devops.model.DevopsWorkIssueWrapper;
import com.coderzoe.component.devops.service.DevopsService;
import com.coderzoe.component.mail.config.MailConfig;
import com.coderzoe.component.mail.service.MailService;
import com.coderzoe.model.request.MailSendRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.coderzoe.common.Constants.DEVOPS_COMPANY_MEMBER_KEY;

/**
 * @author yinhuasheng
 * @date 2024/8/19 19:26
 */
@RestController
@RequestMapping("/devops")
@Setter(onMethod_ = {@Autowired})
public class DevopsController {
    private DevopsService devopsService;
    private MailService mailService;
    private MailConfig mailConfig;

    @Log
    @GetMapping("/sprints-optional")
    public Result<List<DevopsSprintOptionResponse>> getSprintsOptional() {
        return devopsService.getDevopsSprintOptions();
    }

    @Log
    @GetMapping("/request-list")
    public Result<List<DevopsWorkIssueWrapper>> getDevopsRequestList(@RequestParam String sprint) {
        return devopsService.getAndCheckDevopsRequestList(Collections.singletonList(sprint));
    }

    @Log
    @GetMapping("/bug-list")
    public Result<List<DevopsWorkIssueWrapper>> getDevopsBugList(@RequestParam String sprint) {
        return devopsService.getAndCheckDevopsBugList(Collections.singletonList(sprint));
    }

    @Log
    @PostMapping("/send-mail")
    public Result<?> mailSend(@RequestBody List<DevopsWorkIssueWrapper> sendRequestList) {
        List<MailSendRequest> list = parseRequestList(sendRequestList);
        for (MailSendRequest sendRequest : list) {
            DevopsCompanyMember devopsCompanyMember = getByUserName(sendRequest.getUserName());
            if (devopsCompanyMember != null) {
                mailService.sendMail(mailConfig.getUserName(), devopsCompanyMember.getEmail(), sendRequest.getSubject(), sendRequest.getSendContent());
            }
        }
        return Result.success(null);
    }


    private List<MailSendRequest> parseRequestList(List<DevopsWorkIssueWrapper> requestList) {
        List<MailSendRequest> list = new ArrayList<>();
        for (DevopsWorkIssueWrapper request : requestList) {
            if (!request.isSuccess()) {
                list.add(doParse(request));
            } else {
                if (!CollectionUtils.isEmpty(request.getChildren())) {
                    list.addAll(parseRequestList(request.getChildren()));
                }
            }
        }
        //按用户名再汇总：
        list = list.stream()
                .collect(Collectors.groupingBy(MailSendRequest::getUserName))
                .values()
                .stream()
                .map(sendRequestList -> {
                    MailSendRequest first = sendRequestList.getFirst();
                    for (int i = 1; i < sendRequestList.size(); i++) {
                        first.setSendContent(first.getSendContent() + sendRequestList.get(i).getSendContent());
                    }
                    return first;
                })
                .toList();
        return list;
    }

    private MailSendRequest doParse(DevopsWorkIssueWrapper request) {
        StringBuilder builder = new StringBuilder();
        builder.append("[")
                .append(request.getKey())
                .append("]")
                .append(request.getSubject())
                .append("\n");
        builder.append("检查问题：\n");
        request.getSuggest().forEach(suggest -> builder.append(suggest).append("\n"));
        builder.append("====================================================\n");
        return MailSendRequest.builder().userName(request.getAssigneeName())
                .subject("Devops需求/Bug检查")
                .sendContent(builder.toString())
                .build();
    }

    private static DevopsCompanyMember getByUserName(String userName) {
        TypeReference<List<DevopsCompanyMember>> typeRef = new TypeReference<>() {
        };
        List<DevopsCompanyMember> companyMemberList = Cache.getList(DEVOPS_COMPANY_MEMBER_KEY, typeRef);
        if (companyMemberList != null) {
            return companyMemberList.stream()
                    .filter(p -> userName.equals(p.getName()) || userName.equals(p.getShowName()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
