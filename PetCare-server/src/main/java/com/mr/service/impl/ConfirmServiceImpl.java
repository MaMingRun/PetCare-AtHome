package com.mr.service.impl;

import com.mr.common.exception.BaseException;
import com.mr.entity.OrderStatusLog;
import com.mr.entity.Orders;
import com.mr.enums.OrderStatus;
import com.mr.mapper.OrderMapper;
import com.mr.mapper.OrderStatusLogMapper;
import com.mr.service.ConfirmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfirmServiceImpl implements ConfirmService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderStatusLogMapper logMapper;

    @Override
    @Transactional
    public void confirm(Long orderId, Long userId) {
        Orders order = orderMapper.getByIdAndUser(orderId, userId);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        int rows = orderMapper.confirmByUser(orderId);
        if (rows == 0) {
            throw new BaseException("订单当前不可确认（状态已变化）");
        }
        // 状态流转日志
        OrderStatusLog slog = new OrderStatusLog();
        slog.setOrderId(orderId);
        slog.setFromStatus(OrderStatus.PENDING_CONFIRM.name());
        slog.setToStatus(OrderStatus.SETTLING.name());
        slog.setOperatorType("USER");
        slog.setOperatorId(userId);
        slog.setRemark("用户确认收货");
        logMapper.insert(slog);
    }
}
