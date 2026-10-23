package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 订单状态流转日志
 */
@Data
public class OrderStatusLog {

    private Long id;                // 主键ID
    private Long orderId;           // 订单ID，关联Orders主订单
    private String fromStatus;      // 流转前订单状态
    private String toStatus;        // 流转后订单状态
    private String operatorType;     // 操作人类型枚举：USER用户 / PROVIDER服务师 / ADMIN管理员 / SYSTEM系统自动变更
    private Long operatorId;         // 操作人ID，对应当前类型的用户id
    private String remark;           // 状态变更备注（例如：用户取消订单、系统超时关闭、服务师完成服务）
    private LocalDateTime createdAt; // 状态变更发生时间
}