package com.coderzoe.component.devops.service;

import com.coderzoe.common.Constants;
import com.coderzoe.common.Result;
import com.coderzoe.common.exception.CommonException;
import com.coderzoe.common.util.Cache;
import com.coderzoe.component.devops.config.DevopsConfig;
import com.coderzoe.component.devops.model.DevopsLoginRequest;
import com.coderzoe.component.devops.model.DevopsLoginResponse;
import com.coderzoe.component.devops.model.DevopsResult;
import com.coderzoe.component.devops.model.DevopsToken;
import com.coderzoe.service.SystemService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author yinhuasheng
 * @date 2024/8/19 19:27
 */
@Service
@Setter(onMethod = @__(@Autowired))
public class DevopsLoginService {
    private DevopsConfig devopsConfig;
    private SystemService systemService;
    private RestTemplate restTemplate;

    /**
     * 在JDK21中 虚拟线程不支持synchronized，在synchronized内的代码被阻塞无法卸载载体线程
     * 因此改为使用Lock
     */
    private final Lock lock = new ReentrantLock();

    public DevopsToken getDevopsToken() {
        try {
            lock.lock();
            DevopsToken devopsToken = Cache.get(Constants.DEVOPS_TOKEN_KEY, DevopsToken.class);
            if (devopsToken == null) {
                devopsToken = systemService.getByKey(Constants.DEVOPS_TOKEN_KEY, DevopsToken.class);
            }
            if (devopsToken == null) {
                Result<DevopsToken> tokenResult = this.login();
                if (!tokenResult.isSuccess()) {
                    throw new CommonException("devops登录失败，失败原因:" + tokenResult.getMsg());
                }
            } else {
                Cache.set(Constants.DEVOPS_TOKEN_KEY, devopsToken);
            }
            return Cache.get(Constants.DEVOPS_TOKEN_KEY, DevopsToken.class);
        } finally {
            lock.unlock();
        }
    }

    /**
     * devops登录功能
     */
    public Result<DevopsToken> login() {
        DevopsLoginRequest loginRequest = DevopsLoginRequest.builder()
                .userName(devopsConfig.getUserName())
                .password(devopsConfig.getPassword())
                .type(devopsConfig.getLoginType())
                .build();
        ResponseEntity<DevopsResult<DevopsLoginResponse>> loginResult = restTemplate.exchange(devopsConfig.getLoginUrl(),
                HttpMethod.POST,
                new HttpEntity<>(loginRequest),
                new ParameterizedTypeReference<>() {
                }
        );
        if (loginResult.getStatusCode() == HttpStatus.OK) {
            DevopsLoginResponse loginResponse = Objects.requireNonNull(loginResult.getBody()).getData();
            DevopsToken devopsToken = DevopsToken.builder().userName(devopsConfig.getUserName())
                    .userId(loginResponse.getUserId())
                    .userToken(loginResponse.getToken())
                    .build();
            //存token
            systemService.saveOrUpdate(Constants.DEVOPS_TOKEN_KEY, devopsToken);
            Cache.set(Constants.DEVOPS_TOKEN_KEY, devopsToken);
            return Result.success(devopsToken);
        }
        return Result.fail("devops未成功获取到token，响应内容：" + loginResult.getBody());
    }
}
