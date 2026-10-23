package com.mr.entity;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SettlementRule {
    private Long id;
    private String city;
    private BigDecimal commissionRate;
    private String settleCycle;     // T+1 / T+7
    private Integer minWithdrawAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
