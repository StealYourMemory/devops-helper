package com.coderzoe.component.mail.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

/**
 * @author yinhuasheng
 * @date 2024/8/19 10:54
 */
@ConfigurationProperties(prefix = "mail")
@Configuration
@Setter
public class MailConfig {
    @Getter
    private String host;
    @Getter
    private int port;
    @Getter
    private String userName;
    private String password;


    public String getPassword() {
        return new String(Base64.getDecoder().decode(password.getBytes()));
    }
}
