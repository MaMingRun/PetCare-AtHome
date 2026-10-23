package com.mr.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 支付结果视图
 */
@Data
public class OrderPayVO {

    private Long orderId;
    private String orderNo;
    private String status;          //支付后的最新状态（Mock 下应为 PENDING_ACCEPT）
    private Integer payAmount;      //实付金额（分）
    private LocalDateTime payTime;  //支付时间
}
