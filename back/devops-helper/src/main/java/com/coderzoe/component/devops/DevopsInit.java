package com.coderzoe.component.devops;

import com.coderzoe.common.Result;
import com.coderzoe.common.util.Cache;
import com.coderzoe.common.util.JsonUtil;
import com.coderzoe.component.devops.model.DevopsCompanyMember;
import com.coderzoe.component.devops.service.DevopsService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.coderzoe.common.Constants.DEVOPS_COMPANY_MEMBER_KEY;

/**
 * @author yinhuasheng
 * @date 2024/8/21 09:39
 */
@Component
@Slf4j
@Setter(onMethod = @__(@Autowired))
public class DevopsInit implements ApplicationRunner {
    private DevopsService devopsService;

    /**
     * 项目启动时查询公司成员并缓存起来，在邮件发送时使用
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            Result<List<DevopsCompanyMember>> companyMember = devopsService.getCompanyMember();
            if (companyMember.isSuccess()) {
                Cache.set(DEVOPS_COMPANY_MEMBER_KEY, JsonUtil.toJsonString(companyMember.getData()));
            }
        } catch (Exception e) {
            log.error("DevopsInit#run 异常，可能是没配置devops相关功能", e);
        }
    }
}
