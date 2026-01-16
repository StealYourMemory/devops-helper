package com.coderzoe.config;

import com.coderzoe.component.mail.config.MailConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

/**
 * @author yinhuasheng
 * @date 2024/8/19 10:51
 */
@Configuration
@Slf4j
public class BeanConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(5))
                .errorHandler(new HttpErrorHandler())
                .build();
    }


    @Bean
    @Primary
    public MailProperties mailProperties() {
        return new MailProperties();
    }


    @Bean
    public JavaMailSender javaMailSender(MailProperties mailProperties, MailConfig mailConfig) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        // 设置邮件服务器信息
        mailSender.setHost(mailConfig.getHost());
        mailSender.setPort(mailConfig.getPort());
        mailSender.setUsername(mailConfig.getUserName());
        mailSender.setPassword(mailConfig.getPassword());

        // 设置其他的JavaMail属性（例如SMTP的属性）
        Properties properties = new Properties();
        properties.putAll(mailProperties.getProperties());
        mailSender.setJavaMailProperties(properties);

        return mailSender;
    }


    public static class HttpErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            byte[] bytes = new byte[response.getBody().available()];
            //noinspection ResultOfMethodCallIgnored
            response.getBody().read(bytes);
            log.error("请求异常，状态码:{},{},响应结果:{}", response.getStatusCode(), response.getStatusText(), new String(bytes));
        }
    }
}
