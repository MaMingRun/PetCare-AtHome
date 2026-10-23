package com.mr.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性：把 application.yml 中 pet.jwt 前缀的配置绑定成 Java 对象
 */
@Component
@ConfigurationProperties(prefix = "pet.jwt")
@Data
public class JwtProperties {

    private String adminSecretKey;  //管理端密钥
    private Long adminTtl;          //管理端有效期(毫秒)
    private String adminTokenName;  //管理端请求头名称

    private  String userSecretKey;
    private Long userTtl;
    private String userTokenName;

    private  String providerSecretKey;
    private Long providerTtl;
    private String providerTokenName;

}
