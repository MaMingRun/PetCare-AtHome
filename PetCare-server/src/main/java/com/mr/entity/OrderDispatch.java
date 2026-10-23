package com.mr.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 派单记录
 */
@Data
public class OrderDispatch {

    private Long id;                    // 主键ID
    private Long orderId;               // 订单ID，关联Orders主订单
    private Long providerId;            // 被派单的服务师ID，关联Provider
    private String action;              // 本次派单动作枚举：DISPATCH派发 / ACCEPT接单 / REJECT拒单 / REASSIGN改派 / TIMEOUT超时
    private String remark;              // 备注（拒单原因、改派说明等）
    private Long operatorId;            // 操作人ID：后台管理员id；自动派单时可为null
    private LocalDateTime responseDeadline; // 派单响应截止时间，派单后15分钟内服务师必须处理，超时自动失效
    private LocalDateTime createdAt;    // 这条派单记录生成时间
}