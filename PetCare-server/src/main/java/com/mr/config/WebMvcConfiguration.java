package com.mr.config;

import com.mr.common.properties.JwtProperties;
import com.mr.interceptor.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置：注册三端 JWT 拦截器
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //管理端：请求头 token，登录接口放行
        registry.addInterceptor(new JwtTokenInterceptor(
                        jwtProperties.getAdminSecretKey(), jwtProperties.getAdminTokenName(), "empId"))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login");

        //用户端：请求头 authentication
        registry.addInterceptor(new JwtTokenInterceptor(
                        jwtProperties.getUserSecretKey(), jwtProperties.getUserTokenName(), "userId"))
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/auth/login");

        //服务师端：请求头 provider-token
        registry.addInterceptor(new JwtTokenInterceptor(
                        jwtProperties.getProviderSecretKey(), jwtProperties.getProviderTokenName(), "providerId"))
                .addPathPatterns("/provider/**")
                .excludePathPatterns("/provider/auth/login");
    }
}
