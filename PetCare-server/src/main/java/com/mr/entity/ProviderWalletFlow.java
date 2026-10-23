package com.mr.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProviderWalletFlow {
    private Long id;
    private Long providerId;
    private String bizType;         // INCOME_PENDING / INCOME_SETTLED / WITHDRAW_APPLY / WITHDRAW_REJECT / WITHDRAW_PAID
    private Integer amount;
    private Integer balanceAfter;
    private Long bizId;
    private String remark;
    private LocalDateTime createdAt;
}
