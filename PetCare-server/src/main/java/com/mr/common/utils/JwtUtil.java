package com.mr.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类：签发与解析令牌（JJWT 0.11.5 API）
 */
public class JwtUtil {

    /**
     * 生成 JWT
     * @param secretKey 密钥，HS256 要求至少 32 字符
     * @param ttl       有效期（毫秒）
     * @param claims    自定义声明（如 userId）
     */
    public static String createJWT(String secretKey, Long ttl, Map<String, Object> claims) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        JwtBuilder builder = Jwts.builder()
                .signWith(key, SignatureAlgorithm.HS256);
        if (claims != null) {
            builder.setClaims(claims); //注意：必须放在 setExpiration 之前，否则会覆盖掉 exp
        }
        builder.setExpiration(new Date(System.currentTimeMillis() + ttl));
        return builder.compact();
    }

    /**
     * 解析 JWT。密钥不对或已过期会直接抛异常
     */
    public static Claims parseJWT(String secretKey, String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

