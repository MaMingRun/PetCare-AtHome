package com.mr.gateway.impl;

import com.mr.entity.Orders;
import com.mr.gateway.PayGateway;
import com.mr.gateway.PayResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mock 支付网关：不调微信服务器
 * prepay 造一个假预支付单号；confirmPay 直接模拟"微信服务器回调：支付成功"
 */
@Component
@ConditionalOnProperty(name = "pet.pay.mock", havingValue = "true", matchIfMissing = true)
@Slf4j
public class MockPayGateway implements PayGateway {

    @Override
    public String prepay(Orders order) {
        log.info("[Mock支付网关] 统一下单 orderNo={} 金额={}分", order.getOrderNo(), order.getPayAmount());
        return "mockPrepay" + System.currentTimeMillis();
    }

    @Override
    public PayResult confirmPay(Orders order) {
        log.info("[Mock支付网关] 模拟微信回调：订单 {} 支付成功", order.getOrderNo());
        return PayResult.success("4200MOCK" + System.currentTimeMillis());
    }
}