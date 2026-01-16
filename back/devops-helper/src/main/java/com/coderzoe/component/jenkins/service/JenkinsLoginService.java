package com.coderzoe.component.jenkins.service;

import com.coderzoe.common.Result;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.common.util.Cache;
import com.coderzoe.common.util.CookieGetter;
import com.coderzoe.component.jenkins.config.JenkinsConfig;
import com.coderzoe.model.pojo.LoginCookie;
import com.coderzoe.service.SystemService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.coderzoe.common.Constants.JENKINS_COOKIE_KEY;

/**
 * jenkins登录服务
 *
 * @author yinhuasheng
 * @date 2024/8/19 14:56
 */
@Service
@Setter(onMethod = @__(@Autowired))
public class JenkinsLoginService {
    private SystemService systemService;
    private JenkinsConfig jenkinsConfig;
    private RestTemplate restTemplate;
    /**
     * 在JDK21中 虚拟线程不支持synchronized，在synchronized内的代码被阻塞无法卸载载体线程
     * 因此改为使用Lock
     */
    private final Lock lock = new ReentrantLock();

    public LoginCookie getCookie() {
        try {
            lock.lock();
            LoginCookie loginCookie = Cache.get(JENKINS_COOKIE_KEY, LoginCookie.class);
            if (loginCookie == null) {
                loginCookie = systemService.getByKey(JENKINS_COOKIE_KEY, LoginCookie.class);
            }
            if (loginCookie == null) {
                Result<LoginCookie> login = this.login();
                if (!login.isSuccess()) {
                    throw new CommonException("jenkins登录失败，失败原因:" + login.getMsg());
                }
            } else {
                Cache.set(JENKINS_COOKIE_KEY, loginCookie);
            }
            return Cache.get(JENKINS_COOKIE_KEY, LoginCookie.class);
        } finally {
            lock.unlock();
        }
    }

    /**
     * jenkins登录
     */
    public Result<LoginCookie> login() {
        Result<LoginCookie> initCookie = getInitCookie();
        if (!initCookie.isSuccess()) {
            return Result.fail(initCookie.getMsg());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Cookie", initCookie.getData().toString());
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("j_username", jenkinsConfig.getUserName());
        map.add("j_password", jenkinsConfig.getPassword());
        map.add("from", "/");
        map.add("remember_me", "on");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(jenkinsConfig.getLoginUrl(), request, String.class);
        if (response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection()) {
            List<String> cookies = CookieGetter.get(response);
            //先入库 再更新缓存
            LoginCookie loginCookie = new LoginCookie(cookies);
            systemService.saveOrUpdate(JENKINS_COOKIE_KEY, loginCookie);
            Cache.set(JENKINS_COOKIE_KEY, loginCookie);
            return Result.success(loginCookie);
        }
        return Result.fail("未成功获取到jenkins cookie，响应内容：" + response.getBody());
    }

    private Result<LoginCookie> getInitCookie() {
        ResponseEntity<String> response = restTemplate.getForEntity(jenkinsConfig.getInitUrl(), String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return Result.success(new LoginCookie(CookieGetter.get(response)));
        } else {
            return Result.fail("请求jenkins初始页面失败，失败码", response.getStatusCode().toString());
        }
    }
}
