package com.mr.gateway.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr.common.exception.BaseException;
import com.mr.gateway.WechatGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 真实微信网关：jscode2session 换 openid
 * 配置 pet.wechat.mock=false 时生效（个人 AppID 可用，登录不要求企业主体）
 */
@Component
@ConditionalOnProperty(name = "pet.wechat.mock", havingValue = "false")
@Slf4j
public class RealWechatGateway implements WechatGateway {

    @Value("${pet.wechat.appid}")
    private String appid;
    @Value("${pet.wechat.secret}")
    private String secret;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getOpenid(String code) {
        String body = restClient.get()
                .uri("https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code",
                        appid, secret, code)
                .retrieve()
                .body(String.class);
        log.info("[微信网关] jscode2session 响应: {}", body);
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                throw new BaseException("微信登录失败：" + node.get("errmsg").asText());
            }
            return node.get("openid").asText();
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException("微信登录异常，请稍后再试");
        }
    }
}
