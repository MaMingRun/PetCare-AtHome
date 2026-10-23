package com.mr.gateway;

import com.mr.entity.Orders;

/**
 * 支付网关（PRD §11）：无商户号先 Mock，业务代码零感知
 * 与 WechatGateway 同款模式：接口 + 条件装配实现
 */
public interface PayGateway {

    /**
     * 统一下单：创建预支付交易单，返回 prepayId
     */
    String prepay(Orders order);

    /**
     * 确认支付结果：真实实现等微信回调/调查单API；Mock 直接返回成功
     */
    PayResult confirmPay(Orders order);
}
