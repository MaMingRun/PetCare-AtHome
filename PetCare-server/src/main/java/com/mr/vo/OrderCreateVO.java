package com.mr.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderCreateVO {

    private Long orderId;
    private String orderNo;
    private String status;
    private Integer payAmount;
    private LocalDateTime payExpireAt;  //支付截止时间（15分钟后）
}
