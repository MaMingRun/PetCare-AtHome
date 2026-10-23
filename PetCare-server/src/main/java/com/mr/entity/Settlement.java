package com.mr.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Settlement {
    private Long id;
    private Long orderId;
    private Long providerId;
    private Integer orderAmount;
    private Integer platformFee;
    private Integer netAmount;
    private String status;          // PENDING / SETTLED / FROZEN
    private LocalDateTime settledAt;
    private String frozenReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
