package com.mr.mapper;

import com.mr.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentMapper {
    // 根据商户订单号(outTradeNo)查询支付记录
    Payment getByOutTradeNo(String outTradeNo);

    // 新增一条支付单记录
    void insert(Payment payment);

    // 更新预支付ID（重复发起支付时复用订单，只刷新prepayId）
    void updatePrepay(@Param("outTradeNo") String outTradeNo, @Param("prepayId") String prepayId);

    // 支付回调成功后，标记订单状态为成功，保存微信交易号、回调原始数据、支付时间
    void markSuccess(@Param("outTradeNo") String outTradeNo,
                     @Param("transactionId") String transactionId,
                     @Param("callbackRaw") String callbackRaw);
}
