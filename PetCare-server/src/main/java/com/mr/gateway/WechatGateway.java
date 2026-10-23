package com.mr.gateway;

/**
 * 微信能力网关：登录换 openid（PRD §11.3 按接口注入，dev/prod 两套实现）
 */
public interface WechatGateway {

    /**
     * code 换 openid（jscode2session）
     */
    String getOpenid(String code);
}