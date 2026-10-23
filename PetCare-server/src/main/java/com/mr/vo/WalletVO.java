package com.mr.vo;
import lombok.Data;

@Data
public class WalletVO {
    private Long providerId;
    private Integer balance;
    private Integer pendingSettlement;
    private Integer totalIncome;
}
