package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.entity.Orders;
import com.mr.entity.Payment;
import com.mr.enums.OrderStatus;
import com.mr.gateway.PayGateway;
import com.mr.gateway.PayResult;
import com.mr.mapper.OrderMapper;
import com.mr.mapper.PaymentMapper;
import com.mr.service.PayService;
import com.mr.service.support.PayLedger;
import com.mr.vo.OrderPayVO;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@org.springframework.stereotype.Service   //全限定名，避让实体 Service
public class PayServiceImpl implements PayService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PayGateway payGateway;

    @Autowired
    private PayLedger payLedger;

    @Override
    public OrderPayVO pay(Long orderId, Long userId) {
        //校验订单归属与状态
        Orders order = orderMapper.getByIdAndUser(orderId, userId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!OrderStatus.PENDING_PAY.name().equals(order.getStatus())) {
            throw new BaseException("当前订单状态不可支付");
        }
        if (order.getPayExpireAt() != null && order.getPayExpireAt().isBefore(LocalDateTime.now())) {
            throw new BaseException("支付已超时，请重新下单");
        }

        //统一下单（Mock：造假 prepayId；真实：调微信统一下单API）
        String prepayId = payGateway.prepay(order);

        //支付流水（重复点支付复用同一行，靠 out_trade_no 唯一键锚定）
        Payment payment = paymentMapper.getByOutTradeNo(order.getOrderNo());
        if (payment == null) {
            payment = new Payment();
            payment.setOutTradeNo(order.getOrderNo());
            payment.setBizType("ORDER");
            payment.setBizId(order.getId());
            payment.setPrepayId(prepayId);
            payment.setAmount(order.getPayAmount());
            payment.setStatus("CREATED");
            paymentMapper.insert(payment);
        } else {
            paymentMapper.updatePrepay(order.getOrderNo(), prepayId);
        }

        //确认支付结果（Mock：直接成功→立即入账；真实：返回待支付，等微信回调入账）
        PayResult result = payGateway.confirmPay(order);
        if (result.isSuccess()) {
            payLedger.markPaid(order.getOrderNo(), result.getTransactionId(), null);
        }

        //返回最新订单状态
        Orders latest = orderMapper.getById(orderId);
        OrderPayVO vo = new OrderPayVO();
        vo.setOrderId(latest.getId());
        vo.setOrderNo(latest.getOrderNo());
        vo.setStatus(latest.getStatus());
        vo.setPayAmount(latest.getPayAmount());
        vo.setPayTime(latest.getPayTime());
        return vo;
    }
}
