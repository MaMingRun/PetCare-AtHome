package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 支付流水（订单支付 + 保证金缴纳共用，回调按 biz_type 路由）
 */
@Data
public class Payment {

    private Long id;
    private String outTradeNo;       //商户单号（订单支付时 = orderNo，唯一键）
    private String bizType;          //业务类型：ORDER / DEPOSIT
    private Long bizId;              //orders.id 或 provider.id
    private Long providerId;         //DEPOSIT（保证金）时才有值
    private String prepayId;         //预支付单号
    private String transactionId;    //微信支付订单号
    private Integer amount;          //支付金额，单位【分】
    private String status;           //CREATED / SUCCESS / FAILED / CLOSED
    private LocalDateTime paidAt;    //实际支付时间
    private String callbackRaw;      //回调原文留档（数据库 json 类型，实体用 String 接收）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
