package com.mr.service.support;

import com.mr.common.exception.BaseException;
import com.mr.entity.OrderStatusLog;
import com.mr.entity.Orders;
import com.mr.enums.OrderStatus;
import com.mr.mapper.OrderMapper;
import com.mr.mapper.OrderStatusLogMapper;
import com.mr.mapper.PaymentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 支付入账器：/pay 与 /callback/wechat/pay 共用的幂等入账核心
 * 单独成 Bean 的原因：Spring 事务靠代理对象生效，同类内部 this 调用 @Transactional 会静默失效
 */
@Component
@Slf4j
public class PayLedger {

    @Autowired private OrderMapper orderMapper;
    @Autowired private PaymentMapper paymentMapper;
    @Autowired private OrderStatusLogMapper orderStatusLogMapper;

    /**
     * 支付成功入账：订单流转 + 流水落档 + 状态日志
     * 幂等保证：微信会重试回调，重复通知直接当成功返回（否则微信无限重试）
     */
    @Transactional
    public void markPaid(String outTradeNo, String transactionId, String callbackRaw) {
        Orders order = orderMapper.getByOrderNo(outTradeNo);
        if (order == null) {
            throw new BaseException("回调订单不存在：" + outTradeNo);
        }

        //核心幂等：只有当前还是 PENDING_PAY 才允许入账（条件 UPDATE 原子生效）
        int rows = orderMapper.markPaid(outTradeNo,
                OrderStatus.PENDING_PAY.name(), OrderStatus.PENDING_ACCEPT.name());
        if (rows == 0) {
            log.info("[支付入账] 重复通知，忽略。orderNo={} 当前状态={}", outTradeNo, order.getStatus());
            return;
        }

        //支付流水落档
        paymentMapper.markSuccess(outTradeNo, transactionId, callbackRaw);

        //状态流转日志
        OrderStatusLog slog = new OrderStatusLog();
        slog.setOrderId(order.getId());
        slog.setFromStatus(OrderStatus.PENDING_PAY.name());
        slog.setToStatus(OrderStatus.PENDING_ACCEPT.name());
        slog.setOperatorType("SYSTEM");
        slog.setRemark("支付成功，自动入账");
        orderStatusLogMapper.insert(slog);

        log.info("[支付入账] 订单 {} 入账完成，微信单号 {}", outTradeNo, transactionId);
    }
}
