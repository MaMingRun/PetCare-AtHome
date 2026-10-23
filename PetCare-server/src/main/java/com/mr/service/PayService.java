package com.mr.service;

import com.mr.vo.OrderPayVO;

public interface PayService {

    /**
     * 发起支付：校验订单 → 统一下单 → 落支付流水 → 确认结果
     */
    OrderPayVO pay(Long orderId, Long userId);
}
