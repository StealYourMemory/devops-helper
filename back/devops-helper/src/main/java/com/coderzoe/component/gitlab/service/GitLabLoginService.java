package com.coderzoe.component.gitlab.service;

import com.coderzoe.common.Constants;
import com.coderzoe.common.Result;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.common.util.Cache;
import com.coderzoe.common.util.CookieGetter;
import com.coderzoe.common.util.JsonUtil;
import com.coderzoe.component.gitlab.config.GitLabConfig;
import com.coderzoe.component.gitlab.model.AuthKey;
import com.coderzoe.model.pojo.LoginCookie;
import com.coderzoe.service.SystemService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coderzoe.common.Constants.GITLAB_COOKIE_KEY;
import static java.util.regex.Pattern.compile;

/**
 * gitlab登录
 *
 * @author yinhuasheng
 * @date 2024/8/19 14:11
 */
@Service
@Setter(onMethod_ = @Autowired)
public class GitLabLoginService {
    private GitLabConfig gitLabConfig;
    private RestTemplate restTemplate;
    private SystemService systemService;

    /**
     * 在JDK21中 虚拟线程不支持synchronized，在synchronized内的代码被阻塞无法卸载载体线程
     * 因此改为使用Lock
     */
    private final Lock lock = new ReentrantLock();


    /**
     * 获取gitlab cookie 用于后续操作
     *
     * @return 返回cookie
     */
    public LoginCookie getCookie() {
        try {
            lock.lock();
            LoginCookie loginCookie = Cache.get(Constants.GITLAB_COOKIE_KEY, LoginCookie.class);
            if (loginCookie == null) {
                loginCookie = systemService.getByKey(Constants.GITLAB_COOKIE_KEY, LoginCookie.class);
            }
            if (loginCookie == null) {
                Result<LoginCookie> login = this.login();
                if (!login.isSuccess()) {
                    throw new CommonException("gitlab登录失败，失败原因:" + login.getMsg());
                }
            } else {
                Cache.set(GITLAB_COOKIE_KEY, loginCookie);
            }
            return Cache.get(GITLAB_COOKIE_KEY, LoginCookie.class);
        } finally {
            lock.unlock();
        }
    }

    /**
     * gitlab登录 主要是登录页面并获取cookie信息
     * 登录分为两步：先获得auth_key 再通过auth_key+用户名密码登录
     */
    public Result<LoginCookie> login() {
        //1. 拿auth_key
        Result<AuthKey> authKeyResult = getAuthKey();
        if (!authKeyResult.isSuccess()) {
            return Result.fail(authKeyResult.getMsg());
        }
        //2. 认证
        Result<LoginCookie> authenticateResult = authenticate(authKeyResult.getData());
        if (!authenticateResult.isSuccess()) {
            return Result.fail(authenticateResult.getMsg());
        }
        //拿到cookie后更新缓存和库
        //先入库 再更新缓存
        LoginCookie loginCookie = authenticateResult.getData();
        systemService.saveOrUpdate(GITLAB_COOKIE_KEY, loginCookie);
        Cache.set(GITLAB_COOKIE_KEY, loginCookie);
        return Result.success(loginCookie);
    }

    private Result<AuthKey> getAuthKey() {
        ResponseEntity<String> response = restTemplate.getForEntity(gitLabConfig.getLoginUrl(), String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            List<String> cookies = CookieGetter.get(response);
            Pattern pattern = compile(gitLabConfig.getAuthKeyRegex());
            Matcher matcher = pattern.matcher(Objects.requireNonNull(response.getBody()));
            String authKey = matcher.find() ? matcher.group(1) : null;
            if (authKey != null && !CollectionUtils.isEmpty(cookies)) {
                AuthKey auth = AuthKey.builder()
                        .authKey(matcher.group(1))
                        .cookie(CookieGetter.get(cookies.getFirst()))
                        .build();
                return Result.success(auth);
            } else {
                return Result.fail("gitlab的cookie或者authKey不存在,cookie", JsonUtil.toJsonString(cookies), "authKey", authKey);
            }

        } else {
            return Result.fail("gitlab登录失败，失败原因:" + response.getBody());
        }
    }

    private Result<LoginCookie> authenticate(AuthKey authKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Cookie", authKey.getCookie());
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", gitLabConfig.getUserName());
        map.add("password", gitLabConfig.getPassword());
        map.add("authenticity_token", authKey.getAuthKey());
        map.add("remember_me", "1");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(gitLabConfig.getAuthUrl(), request, String.class);
        //这里其实会302跳转到登录页，但我们不太关心，只需要cookie即可，有了这些cookie就可以请求其他
        if (response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection()) {
            List<String> cookies = CookieGetter.get(response);
            return Result.success(new LoginCookie(cookies));
        }
        return Result.fail("未成功获取到gitlab的cookie，响应内容：" + response.getBody());
    }

}
