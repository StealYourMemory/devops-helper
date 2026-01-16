package com.coderzoe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * mvc的配置
 *
 * @author yinhuasheng
 * @date 2024/8/19 11:03
 */
@Configuration
public class MyWebMvcConfig implements WebMvcConfigurer {
    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                //允许哪些URL可以被跨域访问
                .addMapping("/**")
                //是否允许携带凭证信息 如cookie
                .allowCredentials(true)
                //请求源可以是哪些
                .allowedOriginPatterns("*")
                //请求方法可以是哪些
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                //允许请求头可以带哪些东西
                .allowedHeaders("*")
                //可以暴露的服务端的头内容
                //允许请求源可以拿到响应的所有头信息
                .exposedHeaders("*");
    }


    /**
     * SSR 所有/devops-helper/路径都代理到index.html下
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/devops-helper/**").setViewName("forward:/index.html");
    }
}
