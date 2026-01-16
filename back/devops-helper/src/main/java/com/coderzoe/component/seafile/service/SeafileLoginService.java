package com.coderzoe.component.seafile.service;

import com.coderzoe.common.Result;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.common.util.Cache;
import com.coderzoe.common.util.CookieGetter;
import com.coderzoe.component.seafile.config.SeafileConfig;
import com.coderzoe.component.seafile.model.SeafileToken;
import com.coderzoe.model.pojo.LoginCookie;
import com.coderzoe.service.SystemService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coderzoe.common.Constants.SEAFILE_COOKIE_KEY;

/**
 * @author yinhuasheng
 * @date 2024/8/20 10:47
 */
@Service
@Setter(onMethod = @__(@Autowired))
public class SeafileLoginService {
    private SeafileConfig seafileConfig;
    private RestTemplate restTemplate;
    private SystemService systemService;
    /**
     * 在JDK21中 虚拟线程不支持synchronized，在synchronized内的代码被阻塞无法卸载载体线程
     * 因此改为使用Lock
     */
    private final Lock lock = new ReentrantLock();

    public LoginCookie getCookie() {
        try {
            lock.lock();
            LoginCookie loginCookie = Cache.get(SEAFILE_COOKIE_KEY, LoginCookie.class);
            if (loginCookie == null) {
                loginCookie = systemService.getByKey(SEAFILE_COOKIE_KEY, LoginCookie.class);
            }
            if (loginCookie == null) {
                Result<LoginCookie> login = this.login();
                if (!login.isSuccess()) {
                    throw new CommonException("seafile登录失败，失败原因:" + login.getMsg());
                }
            } else {
                Cache.set(SEAFILE_COOKIE_KEY, loginCookie);
            }
            return Cache.get(SEAFILE_COOKIE_KEY, LoginCookie.class);
        } finally {
            lock.unlock();
        }

    }

    public Result<LoginCookie> login() {
        String loginUrl = seafileConfig.getLoginUrl();
        //先拿loginToken
        SeafileToken loginToken = getLoginToken();
        //再做form表单提交
        ResponseEntity<String> response = restTemplate.exchange(loginUrl,
                HttpMethod.POST,
                getRequest(loginToken),
                String.class);
        if (response.getStatusCode().is2xxSuccessful() || response.getStatusCode().is3xxRedirection()) {
            List<String> cookies = CookieGetter.get(response);
            if (!CollectionUtils.isEmpty(cookies)) {
                //先入库 再更新缓存
                cookies.add(loginToken.getCookies().getFirst());
                LoginCookie loginCookie = new LoginCookie(cookies);
                systemService.saveOrUpdate(SEAFILE_COOKIE_KEY, loginCookie);
                Cache.set(SEAFILE_COOKIE_KEY, loginCookie);
                return Result.success(loginCookie);
            }
        }
        return Result.fail("未成功获取到cookie，响应内容：" + response.getBody());
    }

    private HttpEntity<MultiValueMap<String, String>> getRequest(SeafileToken loginToken) {
        HttpHeaders headers = getSeafileHttpHeaders();
        headers.set("Cookie", loginToken.getCookie());
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("login", seafileConfig.getUserName());
        map.add("password", seafileConfig.getPassword());
        map.add("csrfmiddlewaretoken", loginToken.getCsrfMiddlewareToken());
        map.add("next", "/");
        map.add("remember_me", "on");
        return new HttpEntity<>(map, headers);
    }


    public SeafileToken getLoginToken() {
        HttpHeaders headers = getSeafileHttpHeaders();
        ResponseEntity<String> response = restTemplate.exchange(seafileConfig.getLoginTokenUrl(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        String token = null;
        if (response.getStatusCode().is2xxSuccessful()) {
            token = parseGetLoginToken(response.getBody());
        }
        if (token == null) {
            throw new CommonException("seafile获得token失败");
        }
        return SeafileToken.builder()
                .csrfMiddlewareToken(token)
                .cookies(CookieGetter.get(response))
                .build();
    }

    public static String parseGetLoginToken(String html) {
        String regex = "name=\"csrfmiddlewaretoken\"\\s+value=\"([^\"]*)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    public static HttpHeaders getSeafileHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        return headers;
    }
}
