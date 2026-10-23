package com.mr.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProviderWallet {
    private Long providerId;
    private Integer balance;
    private Integer pendingSettlement;
    private Integer frozenDeposit;
    private Integer totalIncome;
    private Integer version;         // 乐观锁
    private LocalDateTime updatedAt;
}
