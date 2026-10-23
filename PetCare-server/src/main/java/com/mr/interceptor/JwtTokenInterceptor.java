package com.mr.interceptor;

import com.mr.common.context.BaseContext;
import com.mr.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 校验拦截器：不注册为 Bean，由 WebMvcConfiguration 按端手动构造（三端参数不同）
 */
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    private final String secretKey;  //该端的签名密钥
    private final String tokenName;  //该端的请求头名称
    private final String claimKey;   //身份id在claims中的键名

    public JwtTokenInterceptor(String secretKey, String tokenName, String claimKey) {
        this.secretKey = secretKey;
        this.tokenName = tokenName;
        this.claimKey = claimKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //非Controller方法（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        String token = request.getHeader(tokenName);
        if (token == null || token.isEmpty()) {
            response.setStatus(401); //未携带令牌
            return false;
        }
        try {
            Claims claims = JwtUtil.parseJWT(secretKey, token);
            Long id = Long.valueOf(claims.get(claimKey).toString());
            BaseContext.setCurrentId(id);
            return true;
        } catch (Exception e) {
            log.warn("令牌校验失败: {}", e.getMessage());
            response.setStatus(401); //伪造/过期/密钥不符
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.remove(); //Tomcat线程池会复用线程，不清理会串号
    }
}