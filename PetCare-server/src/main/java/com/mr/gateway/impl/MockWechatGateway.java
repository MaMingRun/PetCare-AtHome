package com.mr.gateway.impl;

import com.mr.gateway.WechatGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * dev 环境Mock：不调微信服务器，用 code 直接生成一个稳定的假 openid
 * 同一个 code 永远得到同一个 openid，方便反复登录模拟"同一个用户"
 */
@Component
@ConditionalOnProperty(name = "pet.wechat.mock", havingValue = "true", matchIfMissing = true)
@Slf4j
public class MockWechatGateway implements WechatGateway {

    @Override
    public String getOpenid(String code) {
        log.info("[Mock微信网关] code={} 换取 openid", code);
        return "dev-openid-" + code;
    }
}
