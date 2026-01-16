package com.coderzoe.component.jenkins.service;

import com.coderzoe.common.Constants;
import com.coderzoe.common.Result;
import com.coderzoe.common.ThreadPool;
import com.coderzoe.common.enums.BuildStatus;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.common.util.Cache;
import com.coderzoe.common.util.DateUtil;
import com.coderzoe.common.util.JsonUtil;
import com.coderzoe.component.jenkins.config.JenkinsConfig;
import com.coderzoe.component.jenkins.model.BuildHistory;
import com.coderzoe.component.jenkins.model.request.BuildRequest;
import com.coderzoe.component.jenkins.model.request.JenkinsBuildRequest;
import com.coderzoe.component.jenkins.model.response.JenkinsCrumbResponse;
import com.coderzoe.config.ProjectConfig;
import com.coderzoe.model.pojo.LoginCookie;
import com.coderzoe.model.pojo.Project;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yinhuasheng
 * @date 2024/8/19 15:01
 */
@Service
@Setter(onMethod = @__(@Autowired))
@Slf4j
@SuppressWarnings("AlibabaAvoidPatternCompileInMethod")
public class JenkinsBuildService {
    private ProjectConfig projectConfig;
    private JenkinsConfig jenkinsConfig;
    private JenkinsLoginService jenkinsLoginService;
    private RestTemplate restTemplate;

    /**
     * jenkins打包
     */
    public Result<Void> build(BuildRequest request) {
        Result<Void> result = doBuild(request);
        if (!result.isSuccess()) {
            Result<LoginCookie> login = jenkinsLoginService.login();
            if (!login.isSuccess()) {
                throw new CommonException("登录失败，失败原因:" + login.getMsg());
            }
            result = doBuild(request);
        }
        return result;
    }

    private Result<Void> doBuild(BuildRequest request) {
        Project project = projectConfig.get(request.getProjectName());
        //jenkins打包需要一个crumb校验
        Result<JenkinsCrumbResponse> crumb = getCrumb();
        if (!crumb.isSuccess()) {
            return Result.fail(crumb.getMsg());
        }

        //构建打包请求
        LoginCookie loginCookie = jenkinsLoginService.getCookie();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Cookie", loginCookie.toString());
        headers.add("Jenkins-Crumb", crumb.getData().getCrumb());
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("BRANCH", request.getBranch());
        map.add("DOCKER_TAG", request.getTag());
        map.add("statusCode", "303");
        map.add("redirectTo", ".");
        map.add("Jenkins-Crumb", crumb.getData().getCrumb());
        map.add("json", JsonUtil.toJsonString(new JenkinsBuildRequest(request.getBranch(), request.getTag(), crumb.getData().getCrumb())));
        HttpEntity<MultiValueMap<String, String>> buildRequest = new HttpEntity<>(map, headers);

        // 发送 POST 请求
        String url = jenkinsConfig.getBuildUrl(project.getJenkinsUrl());

        ResponseEntity<String> response = restTemplate.postForEntity(url, buildRequest, String.class);
        if (response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection()) {
            return Result.success(null);
        } else {
            return Result.fail("构造请求失败，失败响应码", response.getStatusCode().toString());
        }
    }

    private Result<JenkinsCrumbResponse> getCrumb() {
        String url = jenkinsConfig.getCrumbUrl();
        HttpEntity<String> entity = new HttpEntity<>(getJenkinsHeaders());
        ResponseEntity<JenkinsCrumbResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, JenkinsCrumbResponse.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Result.success(response.getBody());
        }
        return Result.fail("请求crumb失败,可能是http状态码不对,http状态码", response.getStatusCode().toString());
    }

    /**
     * 获取jenkins打包历史
     *
     * @param projectName 项目名
     * @param search      筛选条件
     * @param cached      是否走缓存
     * @return 返回结果
     */
    public Result<List<BuildHistory>> getBuildHistory(String projectName, String search, boolean cached) {
        List<BuildHistory> buildHistoryList;
        String lastTimeStr = Cache.get(Constants.JENKINS_PACKAGE_LAST_TIME_KEY + projectName);

        //如果走缓存 且没有筛选条件 且缓存没失效那么就从缓存中取出
        if (cached
                && !StringUtils.hasLength(search)
                && StringUtils.hasLength(lastTimeStr)
                && System.currentTimeMillis() - Long.parseLong(lastTimeStr) < Constants.JENKINS_HISTORY_CACHE_TIME) {
            TypeReference<List<BuildHistory>> typeRef = new TypeReference<>() {
            };
            buildHistoryList = Cache.getList(Constants.JENKINS_PACKAGE_HISTORY_KEY + projectName, typeRef);
        } else {
            Result<List<BuildHistory>> result = getBuildHistory(projectName, search);
            if (!result.isSuccess()) {
                return result;
            } else {
                buildHistoryList = result.getData();
            }
        }
        return Result.success(buildHistoryList);
    }

    /**
     * 获取jenkins打包记录
     *
     * @param projectName 项目名
     * @param search      筛选条件
     * @return 返回打包记录
     */
    public Result<List<BuildHistory>> getBuildHistory(String projectName, String search) {
        Project project = projectConfig.get(projectName);
        String url = jenkinsConfig.getHistoryUrl(project.getJenkinsUrl());
        if (StringUtils.hasLength(search)) {
            url += ("?search=" + search);
        }
        Result<List<BuildHistory>> result = doGetBuildHistory(url, project.getJenkinsUrl());
        //如果不成功往往代表当前cookie失效，需要重新登录，重新登录后再获取
        if (!result.isSuccess()) {
            Result<LoginCookie> login = jenkinsLoginService.login();
            if (!login.isSuccess()) {
                throw new CommonException("jenkins登录失败，失败原因:" + login.getMsg());
            }
            result = doGetBuildHistory(url, project.getJenkinsUrl());
        }
        //只有在没筛选条件的时候才缓存
        if (result.isSuccess()) {
            //将projectName设置进去
            result.getData().forEach(p -> p.setProjectName(projectName));
            if (!StringUtils.hasLength(search)) {
                //缓存 因为jenkins构建记录可能会频繁查询，避免jenkins被打爆
                Cache.set(Constants.JENKINS_PACKAGE_HISTORY_KEY + projectName, result.getData());
                Cache.set(Constants.JENKINS_PACKAGE_LAST_TIME_KEY + projectName, String.valueOf(System.currentTimeMillis()));
            }
        }
        return result;
    }

    private Result<List<BuildHistory>> doGetBuildHistory(String url, String projectUrl) {
        //拿到cookie然后请求数据
        try {
            HttpEntity<String> entity = new HttpEntity<>(getJenkinsHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return getBuildHistoryParameters(parseBuildHistory(response.getBody()), projectUrl);
            } else {
                return Result.fail("HTTP响应码:", response.getStatusCode().toString(), "可能是由于jenkins登录失效");
            }
        } catch (Exception e) {
            log.error("JenkinsBuildService#doGetBuildHistory 异常", e);
            return Result.fail("请求失败可能是由于jenkins登录失效");
        }
    }

    /**
     * jenkins打包记录中缺少打包的分支和tag信息，这里查接口一起赋值上
     */
    private Result<List<BuildHistory>> getBuildHistoryParameters(List<BuildHistory> buildHistoryList, String projectUrl) {
        try {
            CountDownLatch countDownLatch = new CountDownLatch(buildHistoryList.size());
            for (BuildHistory buildHistory : buildHistoryList) {
                ThreadPool.executorService().submit(() -> {
                    try {
                        LoginCookie loginCookie = jenkinsLoginService.getCookie();
                        HttpHeaders headers = new HttpHeaders();
                        headers.add("Cookie", loginCookie.toString());
                        HttpEntity<String> entity = new HttpEntity<>(headers);
                        String url = jenkinsConfig.getHistoryParametersUrl(projectUrl, buildHistory.getId());
                        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                        if (response.getStatusCode() == HttpStatus.OK) {
                            List<String> parameters = parseBuildHistoryParameters(response.getBody());
                            if (parameters.size() >= 3) {
                                buildHistory.setBranch(parameters.get(1));
                                buildHistory.setTag(parameters.get(2));
                            }
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            //noinspection ResultOfMethodCallIgnored
            countDownLatch.await(20, TimeUnit.SECONDS);
            return Result.success(buildHistoryList);

        } catch (Exception e) {
            log.error("JenkinsBuildService#getBuildHistoryParameters 异常:", e);
            return Result.success(buildHistoryList);
        }
    }

    /**
     * 获取jenkins打包记录详细信息
     *
     * @param projectName 项目名
     * @param buildId     打包id
     * @return 返回详细记录
     */
    public Result<String> getBuildDetail(String projectName, int buildId) {
        Project project = projectConfig.get(projectName);
        String url = jenkinsConfig.getBuildDetailUrl(project.getJenkinsUrl(), buildId);
        Result<String> result = doGetBuildDetail(url);
        if (!result.isSuccess()) {
            Result<LoginCookie> login = jenkinsLoginService.login();
            if (!login.isSuccess()) {
                throw new CommonException("jenkins登录失败，失败原因:" + login.getMsg());
            }
            result = doGetBuildDetail(url);
        }
        return result;
    }

    private Result<String> doGetBuildDetail(String url) {
        //拿到cookie然后请求数据
        try {
            HttpEntity<String> entity = new HttpEntity<>(getJenkinsHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return Result.success(parseBuildDetail(response.getBody()));
            } else {
                return Result.fail("HTTP响应码:", response.getStatusCode().toString(), "可能是由于jenkins登录失效");
            }
        } catch (Exception e) {
            log.error("JenkinsBuildService#doGetBuildDetail 异常:", e);
            return Result.fail("请求失败可能是由于登录失效，请重新登录");
        }
    }

    private HttpHeaders getJenkinsHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", jenkinsLoginService.getCookie().toString());
        return headers;
    }


    /**
     * jenkins 返回的是html，不是前后端分离那种json 因此需要从html中提取出我们需要的数据
     */
    private static List<BuildHistory> parseBuildHistory(String html) {
        List<BuildHistory> buildHistories = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements rows = document.select("tr.build-row");
        for (Element row : rows) {
            String id = row.select("a.model-link.inside.build-link.display-name").text();
            String date = row.select("div.pane.build-details a").text();
            String statusTooltip = row.select("a.build-status-link").attr("tooltip");
            BuildStatus status = BuildStatus.Pending;
            String[] splits = statusTooltip.split(">");
            if (splits.length > 1) {
                status = BuildStatus.valueOf(splits[0].trim().replace("In progress", "Pending"));
            }
            long tookTime = 0;
            long remainingTime = 0;
            // Handle 'tooltip' for 'Success' and 'Failed' status
            if (status != BuildStatus.Pending) {
                String tookTooltip = row.select("div.pane.build-details a").attr("tooltip");
                if (tookTooltip.contains("Took")) {
                    tookTime = parseDuration(tookTooltip.split("Took")[1].trim());
                }
            } else {
                Elements progressList = row.select("div.pane.build-details a");
                if (progressList.size() > 1) {
                    String progressBarTooltip = progressList.get(1).attr("tooltip");
                    if (progressBarTooltip.contains("ago")) {
                        tookTime = parseDuration(progressBarTooltip.split("ago")[0].trim());
                    }
                    if (progressBarTooltip.contains("remaining time:")) {
                        remainingTime = parseDuration(progressBarTooltip.split("remaining time:")[1].trim());
                    }
                }
            }

            buildHistories.add(BuildHistory.builder()
                    .id(Integer.parseInt(id.split("#")[1]))
                    .status(status)
                    .tookTime(tookTime)
                    .remainingTime(remainingTime)
                    .date(DateUtil.formatDate(date).getTime())
                    .build());
        }
        return buildHistories;
    }

    private static List<String> parseBuildHistoryParameters(String html) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("value=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(html);
        // 输出所有匹配的值
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    private static String parseBuildDetail(String html) {
        Pattern pattern = Pattern.compile("<pre[^>]*>(.*?)</pre>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static long parseDuration(String duration) {
        Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)\\s*(min|sec)");
        Matcher matcher = pattern.matcher(duration);
        long totalSeconds = 0;
        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2);
            if ("min".equals(unit)) {
                totalSeconds += Math.round(value * 60);
            } else if ("sec".equals(unit)) {
                totalSeconds += Math.round(value);
            }
        }
        return totalSeconds;
    }
}
